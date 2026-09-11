package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.organigasto.domain.model.TipoMovimiento

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val icono: String,
    val colorHex: String,
    val presupuestoMensual: Double = 0.0,
    val tipo: TipoMovimiento? = TipoMovimiento.GASTO // null significa que aplica para todos
)