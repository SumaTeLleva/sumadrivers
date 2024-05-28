package mx.suma.drivers.repositories.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.database.ScannerDao
import mx.suma.drivers.models.db.ScannerModel
import mx.suma.drivers.repositories.scanner.remote.ScannerRemoteDataSource

class ScannerRepositoryImpl(val remoteDataSource: ScannerRemoteDataSource, val scannerDao: ScannerDao): ScannerRepository {
    override suspend fun obtenerScannerByBitacoraDb(bitacoraId: Long): List<ScannerModel> {
       return withContext(Dispatchers.IO) {
           scannerDao.getScannerByBitacora(bitacoraId)
       }
    }

    override suspend fun guardarScannerDb(scanner: ScannerModel) {
        return withContext(Dispatchers.IO) {
            scannerDao.insert(scanner)
        }
    }

    override suspend fun verifyDataScannerDb(bitacoraId: Long, clienteId: Long, pasajeroId: Long): ScannerModel {
        return withContext(Dispatchers.IO) {
            scannerDao.getScannerDataCondition(bitacoraId, clienteId, pasajeroId)
        }
    }

    override suspend fun guardarScanner(payload: HashMap<String, String>) {
        return remoteDataSource.guardarRegistro(payload)
    }

    override suspend fun eliminarScannerDb(scanner: ScannerModel) {
        return withContext(Dispatchers.IO) {
            scannerDao.delete(scanner)
        }
    }
}