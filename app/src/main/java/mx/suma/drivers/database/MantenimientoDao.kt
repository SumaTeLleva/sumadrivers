package mx.suma.drivers.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import mx.suma.drivers.models.db.MantenimientoModel

@Dao
interface MantenimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: MantenimientoModel)
}