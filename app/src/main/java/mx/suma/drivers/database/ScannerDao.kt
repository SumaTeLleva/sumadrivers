package mx.suma.drivers.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.suma.drivers.models.db.ScannerModel

@Dao
interface ScannerDao {
    @Query("SELECT * FROM scanner")
    fun getScanner(): List<ScannerModel>

    @Query("SELECT * FROM scanner WHERE bitacoraId = (:id)")
    fun getScannerByBitacora(id: Long): List<ScannerModel>

    @Query("SELECT * FROM scanner WHERE bitacoraId = (:bitacoraId) AND clienteId = (:clienteId) AND pasajeroId = (:pasajeroId)")
    fun getScannerDataCondition(bitacoraId: Long, clienteId: Long, pasajeroId:Long): ScannerModel

    @Query("select * from scanner where id = (:id)")
    fun getScannerById(id: Int): ScannerModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scannerDb: ScannerModel)

    @Delete
    fun delete(item: ScannerModel)
    @Query("DELETE from scanner")
    fun cleanAll()
}