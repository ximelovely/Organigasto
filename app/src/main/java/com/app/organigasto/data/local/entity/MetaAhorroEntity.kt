package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "metas_ahorro")
data class MetaAhorroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,           // "Vacaciones", "Laptop nueva"
    val montoObjetivo: Double,
    val montoActual: Double = 0.0,
    val fechaLimite: LocalDate? = null,
    val activa: Boolean = true
)
