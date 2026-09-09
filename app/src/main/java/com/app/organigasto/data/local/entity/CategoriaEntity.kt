package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,          // "Comida", "Transporte", "Vivienda"
    val icono: String,           // nombre del recurso/ícono, ej. "ic_comida"
    val colorHex: String,        // "#8B7BB8"
    val presupuestoMensual: Double = 0.0
)