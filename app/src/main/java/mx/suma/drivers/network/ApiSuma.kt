package mx.suma.drivers.network

import mx.suma.drivers.models.api.*
import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.Pregunta
import mx.suma.drivers.models.api.encuestas.Respuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuestaUnidad
import mx.suma.drivers.models.api.usuarios.Usuario
import mx.suma.drivers.models.api.utils.DirectionResponses
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiSuma {
    @POST("/login")
    suspend fun postLogin(@Body body: RequestBody): Usuario

    @GET("/bitacoras?sort(+hora_arranque)")
    suspend fun getBitacoras(@QueryMap params: Map<String, String>): List<Bitacora>

    @GET("/bitacoras/{id}")
    suspend fun getBitacora(@Path("id") id: Long): Bitacora

    @GET("/bitacoras?confirmado=true&sort(-hora_arranque)&limit(1)")
    suspend fun getUltimoServicio(@Query("id_proveedor") idProveedor: Long, @Query("id_operador") idOperador: Long): List<Bitacora>

    @PUT("/bitacoras/{id}")
    suspend fun putBitacora(@Path("id") id: Long, @Body body: RequestBody): Bitacora

    @GET("/bitacoras/fecha_hora")
    suspend fun getFechaHora(): BitacoraDate

    @POST("/bitacoras/{id}/huellas")
    suspend fun postHuellas(@Path("id") id: Long, @Body body: RequestBody): ResponseBody

    @GET("/directorio")
    suspend fun getDirectorio(): List<Contacto>

    @GET("/tickets")
    suspend fun getTickets(@QueryMap params: Map<String, String>): List<Ticket>

    @POST("/tickets")
    suspend fun postTicket(@Body data: RequestBody): Ticket

    @GET("/unidades/disponibles")
    suspend fun getUnidadesDisponibles(): Unidades

    @GET("/unidades/{id}")
    suspend fun getUnidad(@Path("id") id: Long): Unidad

    @GET("/unidad/kilometraje-rendimiento")
    suspend fun getKilometrajePorUnidad(@QueryMap params: Map<String, String>): KilometrajeUnidad

    @POST("/unidades/{id}/cambiar")
    suspend fun postCambioUnidad(@Path("id") id: Long, @Body body: RequestBody): CambioUnidad

    @GET("/proveedores")
    suspend fun getProveedores(@QueryMap params: Map<String, String>): List<Proveedor>

    @GET("/mantenimientos")
    suspend fun getMantenimientos(@QueryMap params: Map<String, String>): List<Mantenimiento>

    @POST("/mantenimientos")
    suspend fun postMantenimiento(@Body data: RequestBody): Mantenimiento

    @POST("/dispositivos_r")
    suspend fun postDispositivo(@Body data: RequestBody): Dispositivo

    @PUT("/dispositivos_r/{id}")
    suspend fun putDispositivo(@Path("id") id: Long, @Body data: RequestBody): Dispositivo

    @POST("/operadores/{id}/traslado-operacion")
    suspend fun postVisitaOficina(@Path("id") id: Long): ResponseBody

    @POST("/operadores/{id}/visita-taller")
    suspend fun postVisitaTaller(@Path("id") id: Long): ResponseBody

    @GET("/mapas/{id}")
    suspend fun getMapa(@Path("id") id: Long): Mapa

    @GET("/sanitizaciones")
    suspend fun getSanitizaciones(@QueryMap params: Map<String, String>): List<Sanitizacion>

    @POST("/sanitizaciones")
    suspend fun postSanitizacion(@Body data: RequestBody): Sanitizacion

    @GET("/clientes")
    suspend fun getClientes(@QueryMap params: Map<String, String>): List<Cliente>

    @Multipart
    @POST("/media")
    suspend fun postEvidencia(@Part("description") description: RequestBody, @Part file: MultipartBody.Part): ResponseBody

    @GET("/encuestas/{id}")
    suspend fun getEncuesta(@Path("id") id: Long): Encuesta

    @POST("/encuestas/{id}/procesar")
    suspend fun postProcesarEncuesta(@Path("id") id: Long, @Body payload: RequestBody): ResultEncuesta

    @GET("/preguntas")
    suspend fun getPreguntas(@QueryMap params: Map<String, String>): List<Pregunta>

    @GET("/respuestas")
    suspend fun getRespuestas(@QueryMap params: Map<String, String>): List<Respuesta>

    @POST("/respuestas")
    suspend fun postRespuesta(@Body data: RequestBody): Respuesta

    @GET("/archivos/avisos_sumadriver")
    suspend fun getArchivoAvisos(): Avisos

    @GET("/sumadrivers/getEncuestaUnidad")
    suspend fun getEncuestaUnidad():EncuestaUnidad

    @POST("/sumadrivers/AddEncuestaUnidad")
    suspend fun postEncuestaUnidad(@Body parameter: RequestBody): ResultEncuestaUnidad

    @Multipart
    @POST("/sumadrivers/uploadImage")
    suspend fun subirImagen(@Part("id_encuesta") id_encuesta: RequestBody, @Part imagen: MultipartBody.Part): ResponseBody

    @GET("/sumadrivers/getTalleres")
    suspend fun getTallers(@QueryMap params: Map<String, String>): Destino

    @GET
    suspend fun getDirections(@Url url: String ,@QueryMap params: Map<String, String>): Response<DirectionResponses>

    @GET("/sumadrivers/getGasolineras")
    suspend fun getGasolineras():EstacionGas

    @GET("/performance/nuevo_valor_odometro")
    suspend fun saveNewOdometro(@QueryMap params: Map<String, String>): ResSaveNewOdometro

    @POST("/aforo/addRegistroAforo")
    suspend fun registroAforo(@QueryMap params: Map<String, String>): ResponseBody
}