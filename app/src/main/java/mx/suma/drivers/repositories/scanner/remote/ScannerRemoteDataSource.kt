package mx.suma.drivers.repositories.scanner.remote

interface ScannerRemoteDataSource {
    suspend fun guardarRegistro(payload: HashMap<String, String>): Unit
}