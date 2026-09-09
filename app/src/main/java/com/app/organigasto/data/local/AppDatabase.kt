package com.app.organigasto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.organigasto.data.local.dao.MovimientoDao
import com.app.organigasto.data.local.dao.CategoriaDao
import com.app.organigasto.data.local.entity.CategoriaEntity
import com.app.organigasto.data.local.entity.MovimientoEntity

@Database(
    entities = [MovimientoEntity::class, CategoriaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movimientoDao(): MovimientoDao
    abstract fun categoriaDao(): CategoriaDao
}
