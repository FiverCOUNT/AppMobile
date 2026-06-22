# Registra ingresos de inventario con números de serie vía POST /inventario/entradas.
#
# Uso:
#   .\scripts\seed-producto-serie-api.ps1 -Email "tu@correo.pe" -Contrasena "tu_clave"
#   .\scripts\seed-producto-serie-api.ps1 -Email "tu@correo.pe" -Contrasena "tu_clave" -SeriesPorProducto 40 -Lotes 2
#
# Variables de entorno opcionales: SEED_EMAIL, SEED_PASSWORD
#
param(
    [string]$BaseUrl = "http://10.104.199.242:3000/api",
    [string]$Email = $env:SEED_EMAIL,
    [string]$Contrasena = $env:SEED_PASSWORD,
    [string]$CompanyRuc = "",
    [string]$AlmacenId = "",
    [int]$SeriesPorProducto = 40,
    [int]$Lotes = 1,
    [string]$PrefijoSerie = "",
    [switch]$SoloProductoSeries
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Email) -or [string]::IsNullOrWhiteSpace($Contrasena)) {
    throw "Indica -Email y -Contrasena, o define SEED_EMAIL y SEED_PASSWORD."
}

function Test-ProductoSeries {
    param([string]$Nombre, [bool]$ManejaSerie)
    if ($ManejaSerie) { return $true }
    $n = $Nombre.ToLowerInvariant()
    return $n -like '*producto*series*' -or $n -like '*producto*serie*'
}

function Invoke-Api {
    param([string]$Method, [string]$Path, [hashtable]$Headers = @{}, $Body = $null)
    $base = $BaseUrl.TrimEnd('/')
    $pathPart = $Path.TrimStart('/')
    $uri = "$base/$pathPart"
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) { $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress) }
    return Invoke-RestMethod @params
}

function Get-TokenFromLogin {
    param($LoginData)
    if ($LoginData.accessToken) { return $LoginData.accessToken }
    if ($LoginData.token) { return $LoginData.token }
    if ($LoginData.user.accessToken) { return $LoginData.user.accessToken }
    if ($LoginData.user.token) { return $LoginData.user.token }
    throw "Login OK pero no se encontró accessToken en la respuesta."
}

function Get-RucFromLogin {
    param($LoginData, [string]$OverrideRuc)
    if ($OverrideRuc) { return $OverrideRuc }
    if ($LoginData.user.companyRuc) { return $LoginData.user.companyRuc }
    if ($LoginData.user.company.ruc) { return $LoginData.user.company.ruc }
    throw "No se pudo obtener el RUC de la sesión."
}

$stamp = Get-Date -Format "yyyyMMddHHmmss"
$serieRun = if ($PrefijoSerie) { $PrefijoSerie.Trim() } else { $stamp }

Write-Host "Login en $BaseUrl ..." -ForegroundColor Cyan
$login = Invoke-Api -Method Post -Path "auth/login" -Body @{
    email      = $Email
    contrasena = $Contrasena
}
if (-not $login.success) { throw "Login falló: $($login.message)" }

$data = $login.data
$token = Get-TokenFromLogin $data
$ruc = Get-RucFromLogin $data $CompanyRuc
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Empresa RUC: $ruc" -ForegroundColor Green

Write-Host "Listando almacenes..." -ForegroundColor Cyan
$almacenes = @(Invoke-Api -Method Get -Path "empresas/$ruc/almacenes" -Headers $headers)
if ($almacenes.Count -eq 0) { throw "La empresa no tiene almacenes." }

$almId = if ($AlmacenId) {
    $AlmacenId
} elseif ($data.almacenId) {
    $data.almacenId
} elseif ($data.user.almacenId) {
    $data.user.almacenId
} else {
    $almacenes[0].id
}
$almNombre = ($almacenes | Where-Object { $_.id -eq $almId } | Select-Object -First 1).nombre
if (-not $almNombre) { $almNombre = $almacenes[0].nombre }
Write-Host "Almacén: $almNombre ($almId)" -ForegroundColor Green

Write-Host "Listando catálogo..." -ForegroundColor Cyan
$catalogo = @(Invoke-Api -Method Get -Path "empresas/$ruc/catalogo?almacen_id=$almId" -Headers $headers)
$conSerie = @($catalogo | Where-Object {
    if ($SoloProductoSeries) {
        $_.nombre.ToLowerInvariant() -like '*producto*series*' -or $_.nombre.ToLowerInvariant() -like '*producto*serie*'
    } else {
        Test-ProductoSeries $_.nombre $_.maneja_serie
    }
})

if ($conSerie.Count -eq 0) {
    Write-Warning "No hay ítems serializados en el catálogo del almacén seleccionado."
    exit 0
}

Write-Host "Productos serializados: $($conSerie.Count) | Series por lote: $SeriesPorProducto | Lotes: $Lotes" -ForegroundColor Cyan

$totalOk = 0
$totalErr = 0

foreach ($item in $conSerie) {
    $idCompact = ($item.id -replace '-', '')
    $prefijo = $idCompact.Substring(0, [Math]::Min(8, $idCompact.Length)).ToUpper()

    for ($lote = 1; $lote -le $Lotes; $lote++) {
        $numeros = 1..$SeriesPorProducto | ForEach-Object {
            "SN-$prefijo-$serieRun-L$lote-{0:D3}" -f $_
        }

        Write-Host "Ingreso lote $lote/$Lotes -> $($item.nombre) ($($numeros.Count) series)" -ForegroundColor Yellow

        $body = @{
            almacen_id    = $almId
            observaciones = "Seed producto_serie $serieRun lote $lote"
            lineas        = @(
                @{
                    catalog_item_id = $item.id
                    cantidad        = $SeriesPorProducto
                    series          = $numeros
                }
            )
        }

        try {
            $resp = Invoke-Api -Method Post -Path "empresas/$ruc/inventario/entradas" -Headers $headers -Body $body
            if ($resp.success -eq $false) {
                $msg = if ($resp.message) { $resp.message } else { "Respuesta success=false" }
                throw $msg
            }
            Write-Host "  OK ($($numeros[0]) ... $($numeros[-1]))" -ForegroundColor Green
            $totalOk++
        } catch {
            Write-Warning "  Error lote $lote: $($_.Exception.Message)"
            $totalErr++
        }
    }
}

Write-Host "`nResumen: $totalOk ingreso(s) OK, $totalErr error(es)." -ForegroundColor Cyan
Write-Host "Verifica en la app: Catálogo -> producto con serie -> stock disponible." -ForegroundColor Cyan
