package mx.suma.drivers.repositories.scanner.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.network.ApiSuma

class ScannerRemoteDataSourceImpl(val apiSuma: ApiSuma):ScannerRemoteDataSource {
    override suspend fun guardarRegistro(payload: HashMap<String, String>): Unit {
        return withContext(Dispatchers.IO) {
            apiSuma.registroAforo(payload)
        }
    }
}