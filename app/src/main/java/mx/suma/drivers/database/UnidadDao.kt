package mx.suma.drivers.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.suma.drivers.models.db.UnidadModel

@Dao
interface UnidadDao {
    @Query("select * from unidades")
    fun getUnidades(): LiveData<List<UnidadModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: UnidadModel)

    @Query("DELETE FROM unidades")
    fun deleteAll()
}