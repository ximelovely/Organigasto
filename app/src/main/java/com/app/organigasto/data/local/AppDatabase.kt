package com.app.organigasto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.organigasto.data.local.dao.*
import com.app.organigasto.data.local.entity.*

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
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun presupuestoPeriodoDao(): PresupuestoPeriodoDao
    abstract fun gastoDao(): GastoDao
    abstract fun suscripcionDao(): SuscripcionDao
    abstract fun cobroSuscripcionDao(): CobroSuscripcionDao
    abstract fun prestamoDao(): PrestamoDao
    abstract fun pagoPrestamoDao(): PagoPrestamoDao
}
