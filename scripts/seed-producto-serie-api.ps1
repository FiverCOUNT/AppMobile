# Crea series en el backend vía POST /inventario/entradas (ingreso manual).
# Uso:
#   .\scripts\seed-producto-serie-api.ps1 -Email "tu@correo.pe" -Contrasena "tu_clave"
#
param(
    [string]$BaseUrl = "http://172.20.10.11:3000/api",
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Contrasena,
    [string]$CompanyRuc = "",
    [string]$AlmacenId = "",
    [int]$SeriesPorProducto = 20
)

$ErrorActionPreference = "Stop"

function Test-ProductoSeries {
    param([string]$Nombre, [bool]$ManejaSerie)
    if ($ManejaSerie) { return $true }
    $n = $Nombre.ToLowerInvariant()
    return $n -like '*producto*series*' -or $n -like '*producto*serie*'
}

function Invoke-Api {
    param([string]$Method, [string]$Path, [hashtable]$Headers = @{}, $Body = $null)
    $uri = "$BaseUrl/$Path".Replace("//", "/").Replace(":/", "://")
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $Headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) { $params.Body = ($Body | ConvertTo-Json -Depth 8) }
    return Invoke-RestMethod @params
}

Write-Host "Login..." -ForegroundColor Cyan
$login = Invoke-Api -Method Post -Path "auth/login" -Body @{
    email      = $Email
    contrasena = $Contrasena
}
if (-not $login.success) { throw "Login falló: $($login.message)" }
$token = $login.data.token
$ruc = if ($CompanyRuc) { $CompanyRuc } else { $login.data.company.ruc }
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Empresa RUC: $ruc" -ForegroundColor Green

Write-Host "Listando almacenes..." -ForegroundColor Cyan
$almacenes = Invoke-Api -Method Get -Path "empresas/$ruc/almacenes" -Headers $headers
$almId = if ($AlmacenId) { $AlmacenId } else { $almacenes[0].id }
Write-Host "Almacén: $($almacenes[0].nombre) ($almId)" -ForegroundColor Green

Write-Host "Listando catálogo..." -ForegroundColor Cyan
$catalogo = Invoke-Api -Method Get -Path "empresas/$ruc/catalogo?almacen_id=$almId" -Headers $headers
$conSerie = @($catalogo | Where-Object { Test-ProductoSeries $_.nombre $_.maneja_serie })
if ($conSerie.Count -eq 0) {
    Write-Warning "No hay ítems serializados (maneja_serie o nombre 'Producto Series')."
    exit 0
}

foreach ($item in $conSerie) {
    $prefijo = ($item.id -replace '-', '').Substring(0, [Math]::Min(8, ($item.id -replace '-', '').Length)).ToUpper()
    $numeros = 1..$SeriesPorProducto | ForEach-Object {
        "SN-$prefijo-{0:D3}" -f $_
    }
    Write-Host "Ingreso series para: $($item.nombre) -> $($numeros -join ', ')" -ForegroundColor Yellow
    $body = @{
        company_ruc   = $ruc
        almacen_id    = $almId
        observaciones = "Seed producto_serie desde script"
        lineas        = @(
            @{
                catalog_item_id = $item.id
                cantidad        = $SeriesPorProducto
                series          = $numeros
            }
        )
    }
    try {
        Invoke-Api -Method Post -Path "empresas/$ruc/inventario/entradas" -Headers $headers -Body $body | Out-Null
        Write-Host "  OK" -ForegroundColor Green
    } catch {
        Write-Warning "  Error: $($_.Exception.Message)"
    }
}

Write-Host "`nVerifica en la app: Catálogo -> toca un producto con serie." -ForegroundColor Cyan
