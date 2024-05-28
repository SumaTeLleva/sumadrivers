package mx.suma.drivers.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import mx.suma.drivers.models.db.BitacoraModel

@Dao
interface BitacorasDao {
    @Query("SELECT * FROM bitacoras")
    fun getBitacoras(): List<BitacoraModel>

    @Query("SELECT * FROM bitacoras WHERE id = (:id)")
    fun getBitacorasById(id: Long): BitacoraModel

    @Query("DELETE from bitacoras")
    fun cleanAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: BitacoraModel)

    @Insert
    fun insert(item:BitacoraModel)

    @Update
    fun update(item:BitacoraModel)
}