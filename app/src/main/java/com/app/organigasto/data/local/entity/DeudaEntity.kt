package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "deudas")
data class DeudaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val persona: String,
    val monto: Double,
    val fecha: LocalDate,
    val fechaLimite: LocalDate? = null,
    val esPrestamo: Boolean, // true = yo presté, false = yo debo
    val liquidada: Boolean = false,
    val nota: String? = null
)
