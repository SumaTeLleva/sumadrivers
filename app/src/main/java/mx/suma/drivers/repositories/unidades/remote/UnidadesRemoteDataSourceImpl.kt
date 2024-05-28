package mx.suma.drivers.repositories.unidades.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.CambioUnidad
import mx.suma.drivers.models.api.KilometrajeUnidad
import mx.suma.drivers.models.api.ResSaveNewOdometro
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.Unidades
import mx.suma.drivers.network.ApiSuma
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

class UnidadesRemoteDataSourceImpl(val apiSuma: ApiSuma) : UnidadesRemoteDataSource {
    override suspend fun buscarUnidadesDisponibles(): Unidades {
        return withContext(Dispatchers.IO) {
            apiSuma.getUnidadesDisponibles()
        }
    }

    override suspend fun buscarUnidad(id: Long): Unidad {
        return withContext(Dispatchers.IO) {
            apiSuma.getUnidad(id)
        }
    }

    override suspend fun cambiarUnidad(id: Long, payload: JSONObject): CambioUnidad {
        return withContext(Dispatchers.IO) {
            val body = RequestBody.create(
                MediaType.get("application/json"), payload.toString()
            )

            apiSuma.postCambioUnidad(id, body)
        }
    }

    override suspend fun obtenerKilometrajePorUnidad(params: HashMap<String, String>): KilometrajeUnidad {
        return withContext(Dispatchers.IO) {
            apiSuma.getKilometrajePorUnidad(params)
        }
    }

    override suspend fun guardarNuevoKilometraje(params: HashMap<String, String>): ResSaveNewOdometro {
        return withContext(Dispatchers.IO) {
            apiSuma.saveNewOdometro(params)
        }
    }
}