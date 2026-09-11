package com.app.organigasto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.organigasto.data.local.dao.*
import com.app.organigasto.data.local.entity.*

@Database(
    entities = [
        MovimientoEntity::class, 
        CategoriaEntity::class, 
        UserEntity::class, 
        CuentaEntity::class, 
        MetaAhorroEntity::class,
        SuscripcionEntity::class,
        DeudaEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movimientoDao(): MovimientoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun userDao(): UserDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun metaAhorroDao(): MetaAhorroDao
    abstract fun suscripcionDao(): SuscripcionDao
    abstract fun deudaDao(): DeudaDao
}
