package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.organigasto.domain.model.*

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val salt: String
)

@Entity(
    tableName = "categoria",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val colorHex: String? = null,
    val iconoNombre: String? = null,
    val iconoUri: String? = null
)

@Entity(
    tableName = "presupuesto_periodo",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoriaId"])]
)
data class PresupuestoPeriodo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoriaId: Int,
    val tipoPeriodo: String,
    val fechaInicio: Long,
    val fechaFin: Long,
    val montoAsignado: Double
)

@Entity(
    tableName = "gasto",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"]), Index(value = ["categoriaId"])]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val categoriaId: Int,
    val monto: Double,
    val fecha: String,
    val descripcion: String,
    val cuentaPago: String? = "Débito"
)

@Entity(
    tableName = "suscripcion",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["usuarioId"]), Index(value = ["categoriaId"])]
)
data class Suscripcion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val categoriaId: Int?,
    val nombre: String,
    val costo: Double,
    val fechaProximoCobro: String,
    val esAutomatica: Boolean,
    val frecuencia: String,
    val estado: String,
    val activarRecordatorio: Boolean,
    val diasAviso: Int
)

@Entity(
    tableName = "cobro_suscripcion",
    foreignKeys = [
        ForeignKey(
            entity = Suscripcion::class,
            parentColumns = ["id"],
            childColumns = ["suscripcionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["suscripcionId"])]
)
data class CobroSuscripcion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val suscripcionId: Int,
    val fecha: String,
    val monto: Double,
    val estado: String
)

@Entity(
    tableName = "prestamo",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class Prestamo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val montoOriginal: Double,
    val meses: Int,
    val tipo: String,
    val tasaAnual: Double,
    val fechaInicio: String,
    val activarRecordatorio: Boolean,
    val diasAviso: Int,
    val categoriaId: Int? = null,
    val diaPagoMensual: Int? = null,
    val fechaVencimiento: String? = null
)

@Entity(
    tableName = "pago_prestamo",
    foreignKeys = [
        ForeignKey(
            entity = Prestamo::class,
            parentColumns = ["id"],
            childColumns = ["prestamoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["prestamoId"])]
)
data class PagoPrestamo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val prestamoId: Int,
    val numeroPago: Int,
    val pagoMensual: Double,
    val interesPagado: Double,
    val capitalPagado: Double,
    val saldoRestante: Double,
    val pagado: Boolean
)
