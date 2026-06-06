package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Almacen
import com.factapp.jhonny.network.dto.request.CrearAlmacenRequest
import com.factapp.jhonny.network.dto.model.CatalogItem
import com.factapp.jhonny.network.dto.model.Cliente
import com.factapp.jhonny.network.dto.request.ActualizarCatalogItemRequest
import com.factapp.jhonny.network.dto.request.CrearCatalogItemRequest
import com.factapp.jhonny.network.dto.request.CrearClienteRequest
import com.factapp.jhonny.network.dto.request.PatchCatalogItemRequest
import com.factapp.jhonny.network.dto.model.InventarioSaldo
import com.factapp.jhonny.network.dto.model.Invoice
import com.factapp.jhonny.network.dto.model.Movimiento
import com.factapp.jhonny.network.dto.request.RegistrarSalidaRequest
import com.factapp.jhonny.network.dto.ApiEnvelope
import com.factapp.jhonny.network.dto.model.UsuarioSesionApi
import com.factapp.jhonny.network.dto.request.LoginApiRequest
import com.factapp.jhonny.network.dto.request.RefreshTokenRequest
import com.factapp.jhonny.network.dto.request.EmitirComprobanteRequest
import com.factapp.jhonny.network.dto.model.ProductoSerie
import com.factapp.jhonny.network.dto.request.RegistrarEntradaRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginApiRequest): ApiEnvelope<UsuarioSesionApi>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): ApiEnvelope<UsuarioSesionApi>

    /** Catálogo de productos/servicios de la empresa (origen: servidor). */
    /** Clientes de la empresa emisora (personas y empresas receptoras). */
    @GET("empresas/{ruc}/clientes")
    suspend fun listarClientes(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
    ): List<Cliente>

    @POST("empresas/{ruc}/clientes")
    suspend fun crearCliente(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: CrearClienteRequest,
    ): Cliente

    @GET("empresas/{ruc}/catalogo")
    suspend fun listarCatalogo(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Query("almacen_id") almacenId: String? = null,
    ): List<CatalogItem>

    @POST("empresas/{ruc}/catalogo")
    suspend fun crearCatalogItem(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: CrearCatalogItemRequest,
    ): CatalogItem

    @PUT("empresas/{ruc}/catalogo/{id}")
    suspend fun actualizarCatalogItem(
        @Path("ruc") ruc: String,
        @Path("id") id: String,
        @Header("Authorization") authorization: String,
        @Body body: ActualizarCatalogItemRequest,
    ): CatalogItem

    @PATCH("empresas/{ruc}/catalogo/{id}")
    suspend fun patchCatalogItem(
        @Path("ruc") ruc: String,
        @Path("id") id: String,
        @Header("Authorization") authorization: String,
        @Body body: PatchCatalogItemRequest,
    ): CatalogItem

    @DELETE("empresas/{ruc}/catalogo/{id}")
    suspend fun eliminarCatalogItem(
        @Path("ruc") ruc: String,
        @Path("id") id: String,
        @Header("Authorization") authorization: String,
    ): ApiEnvelope<Any?>

    /** Emite y persiste un comprobante; devuelve el documento con serie, número y estado. */
    @POST("empresas/{ruc}/comprobantes")
    suspend fun guardarComprobante(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: EmitirComprobanteRequest,
    ): Invoice

    /** Comprobantes emitidos por la empresa (ventas). */
    @GET("empresas/{ruc}/comprobantes")
    suspend fun listarComprobantesEmitidos(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
    ): List<Invoice>

    /** Facturas de compra registradas (comprobantes de proveedores). */
    @GET("empresas/{ruc}/compras")
    suspend fun listarCompras(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
    ): List<Invoice>

    @GET("empresas/{ruc}/almacenes")
    suspend fun listarAlmacenes(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Query("solo_activos") soloActivos: Boolean? = null,
    ): List<Almacen>

    @POST("empresas/{ruc}/almacenes")
    suspend fun crearAlmacen(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: CrearAlmacenRequest,
    ): Almacen

    @GET("empresas/{ruc}/catalogo/{catalogItemId}/series-disponibles")
    suspend fun listarSeriesDisponibles(
        @Path("ruc") ruc: String,
        @Path("catalogItemId") catalogItemId: String,
        @Header("Authorization") authorization: String,
        @Query("almacen_id") almacenId: String? = null,
    ): List<ProductoSerie>

    /** Saldos de inventario por producto y almacén (`inventarioApiController.list`). */
    @GET("empresas/{ruc}/inventario")
    suspend fun listarInventario(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Query("almacen_id") almacenId: String? = null,
        @Query("catalog_item_id") catalogItemId: String? = null,
        @Query("solo_con_stock") soloConStock: Boolean? = null,
    ): List<InventarioSaldo>

    @GET("empresas/{ruc}/inventario/{id}")
    suspend fun obtenerInventarioPorId(
        @Path("ruc") ruc: String,
        @Path("id") id: String,
        @Header("Authorization") authorization: String,
    ): InventarioSaldo

    @POST("empresas/{ruc}/inventario/entradas")
    suspend fun registrarEntrada(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: RegistrarEntradaRequest,
    ): Movimiento

    @GET("empresas/{ruc}/inventario/movimientos")
    suspend fun listarMovimientos(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Query("tipo") tipo: String? = null,
    ): List<Movimiento>

    /** Salidas / entregas ([Movimiento] tipo SALIDA). Misma ruta legacy `/entregas`. */
    @GET("empresas/{ruc}/entregas")
    suspend fun listarSalidas(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
    ): List<Movimiento>

    @POST("empresas/{ruc}/entregas")
    suspend fun registrarSalida(
        @Path("ruc") ruc: String,
        @Header("Authorization") authorization: String,
        @Body body: RegistrarSalidaRequest,
    ): Movimiento
}
