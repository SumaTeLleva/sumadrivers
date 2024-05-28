package mx.suma.drivers.database

import androidx.lifecycle.LiveData
import androidx.room.*
import mx.suma.drivers.models.db.UsuarioModel

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(usuarioDb: UsuarioModel)

    @Query("select * from usuarios limit 1")
    fun getUsuario(): LiveData<UsuarioModel>

    @Query("delete from usuarios")
    fun clear()
}