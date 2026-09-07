package com.example.organigasto

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        PresupuestoPeriodo::class,
        Gasto::class,
        Suscripcion::class,
        CobroSuscripcion::class,
        Prestamo::class,
        PagoPrestamo::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OrganigastoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun presupuestoPeriodoDao(): PresupuestoPeriodoDao
    abstract fun gastoDao(): GastoDao
    abstract fun suscripcionDao(): SuscripcionDao
    abstract fun cobroSuscripcionDao(): CobroSuscripcionDao
    abstract fun prestamoDao(): PrestamoDao
    abstract fun pagoPrestamoDao(): PagoPrestamoDao

    companion object {
        @Volatile
        private var INSTANCE: OrganigastoDatabase? = null

        fun getInstance(context: Context): OrganigastoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OrganigastoDatabase::class.java,
                    "organigasto.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
