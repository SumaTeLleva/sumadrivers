package mx.suma.drivers.repositories.unidades

import mx.suma.drivers.models.api.CambioUnidad
import mx.suma.drivers.models.api.KilometrajeUnidad
import mx.suma.drivers.models.api.ResSaveNewOdometro
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.Unidades
import org.json.JSONObject

interface UnidadesRepository {

    /*val unidades: LiveData<List<UnidadModel>>

    suspend fun bajarUnidadesSumaActivas(force: Boolean = false)*/

    suspend fun bajarUnidad(id: Long): Unidad

    suspend fun buscarUnidadesDisponibles():Unidades

    suspend fun cambiarUnidad(id: Long, payload: JSONObject): CambioUnidad

    suspend fun obtenerKilometrajePorUnidad(id: Long): KilometrajeUnidad

    suspend fun guardarNuevoKilometraje(idUnidad:Long, value:Long): ResSaveNewOdometro

}