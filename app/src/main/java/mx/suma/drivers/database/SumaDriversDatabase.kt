package mx.suma.drivers.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.suma.drivers.models.db.*

@Database(entities = [
    UsuarioModel::class,
    MantenimientoModel::class,
    ContactoModel::class,
    UnidadModel::class,
    ProveedorModel::class,
    ScannerModel::class,
    BitacoraModel::class,
], version = 1, exportSchema = false)
abstract class SumaDriversDatabase : RoomDatabase() {

    abstract val usuarioDao: UsuarioDao
    abstract val directorioDao: DirectorioDao
    abstract val mantenimientoDao: MantenimientoDao
    abstract val unidadDao: UnidadDao
    abstract val proveedorDao: ProveedorDao
    abstract val scannerDao: ScannerDao
    abstract val bitacoraDao: BitacorasDao

    companion object {
        @Volatile
        private var INSTANCE: SumaDriversDatabase? = null

        fun getInstance(context: Context): SumaDriversDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SumaDriversDatabase::class.java,
                        "sumadrivers_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}