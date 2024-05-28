package mx.suma.drivers.repositories.scanner

import mx.suma.drivers.models.db.ScannerModel

interface ScannerRepository {
    suspend fun obtenerScannerByBitacoraDb(bitacoraId: Long): List<ScannerModel>
    suspend fun guardarScannerDb(scanner:ScannerModel)
    suspend fun verifyDataScannerDb(bitacoraId: Long, clienteId: Long, pasajeroId:Long): ScannerModel
    suspend fun guardarScanner(payload: HashMap<String, String>)
    suspend fun eliminarScannerDb(scanner: ScannerModel)
}