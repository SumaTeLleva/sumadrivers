package mx.suma.drivers.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.suma.drivers.models.db.ProveedorModel

@Dao
interface ProveedorDao {
    @Query("select * from proveedores where idCategoria = 1 order by nombre")
    fun getGasolineras(): LiveData<List<ProveedorModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: ProveedorModel)

    @Query("DELETE FROM proveedores")
    fun deleteAll()
}