package mx.suma.drivers.repositories.unidades.remote

import mx.suma.drivers.models.api.CambioUnidad
import mx.suma.drivers.models.api.KilometrajeUnidad
import mx.suma.drivers.models.api.ResSaveNewOdometro
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.Unidades
import org.json.JSONObject


interface UnidadesRemoteDataSource {
    suspend fun buscarUnidadesDisponibles(): Unidades

    suspend fun buscarUnidad(id: Long): Unidad

    suspend fun cambiarUnidad(id: Long, payload: JSONObject): CambioUnidad

    suspend fun obtenerKilometrajePorUnidad(params: HashMap<String, String>): KilometrajeUnidad

    suspend fun guardarNuevoKilometraje(params: HashMap<String, String>): ResSaveNewOdometro
}