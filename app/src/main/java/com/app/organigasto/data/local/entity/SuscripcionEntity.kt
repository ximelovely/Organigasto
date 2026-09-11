package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "suscripciones")
data class SuscripcionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val monto: Double,
    val proximoPago: LocalDate,
    val categoriaId: Long,
    val activa: Boolean = true
)
