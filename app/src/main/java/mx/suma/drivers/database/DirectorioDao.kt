package mx.suma.drivers.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.suma.drivers.models.db.ContactoModel

@Dao
interface DirectorioDao {
    @Query("select * from directorio")
    fun getContactos(): LiveData<List<ContactoModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: ContactoModel)

    @Query("DELETE from directorio")
    fun cleanAll()
}