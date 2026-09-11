package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,           // "Tarjeta BBVA", "Efectivo"
    val tipo: String,             // "Debito", "Credito", "Efectivo"
    val saldoInicial: Double,
    val saldoActual: Double,
    val colorHex: String = "#8B7BB8"
)
