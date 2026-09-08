package com.example.organigasto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Nota: TipoPrestamo se reutiliza del archivo FinancialCalculator.kt, para que la
// base de datos y los cálculos usen exactamente los mismos valores
// (SIN_INTERESES / CON_INTERES) sin duplicar el enum.

// ---------------------------------------------------------------
// ENUMS propios de la base de datos
// (evitan strings libres y sus errores de dedo, ej. "Enero" vs "enero")
// ---------------------------------------------------------------

enum class TipoPeriodo { SEMANAL, QUINCENAL, MENSUAL }
enum class FrecuenciaSuscripcion { MENSUAL, ANUAL }
enum class EstadoSuscripcion { ACTIVA, PAUSADA, CANCELADA }
enum class EstadoCobro { PENDIENTE, COBRADO, FALLIDO }

// ---------------------------------------------------------------
// USUARIO
// ---------------------------------------------------------------

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val salt: String
)

// ---------------------------------------------------------------
// CATEGORIA (estable: sin monto ni mes - eso vive en PresupuestoPeriodo)
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val usuarioId: Long,
    val nombre: String
)

// ---------------------------------------------------------------
// PRESUPUESTO_PERIODO (soporta semanal, quincenal o mensual)
// ---------------------------------------------------------------

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
    indices = [Index(value = ["categoriaId", "fechaInicio", "tipoPeriodo"], unique = true)]
)
data class PresupuestoPeriodo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoriaId: Long,
    val tipoPeriodo: TipoPeriodo,
    val fechaInicio: Long,   // timestamp en milisegundos (epoch)
    val fechaFin: Long,      // timestamp en milisegundos (epoch)
    val montoAsignado: Double
)

// ---------------------------------------------------------------
// GASTO (usuarioId denormalizado desde categoria.usuarioId al insertar)
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val usuarioId: Long,     // NUNCA la pone la UI: se copia de categoria.usuarioId en el repositorio
    val categoriaId: Long,
    val monto: Double,
    val fecha: Long,         // timestamp en milisegundos
    val descripcion: String
)

// ---------------------------------------------------------------
// SUSCRIPCION
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val usuarioId: Long,
    val categoriaId: Long?,        // opcional: puede no clasificarse en el organigasto
    val nombre: String,
    val costo: Double,
    val fechaProximoCobro: Long,   // timestamp en milisegundos
    val esAutomatica: Boolean,
    val frecuencia: FrecuenciaSuscripcion,
    val estado: EstadoSuscripcion,
    val activarRecordatorio: Boolean,
    val diasAviso: Int
)

// ---------------------------------------------------------------
// COBRO_SUSCRIPCION (historial de cada cobro real, como PAGO_PRESTAMO)
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val suscripcionId: Long,
    val fecha: Long,          // timestamp en milisegundos
    val monto: Double,
    val estado: EstadoCobro
)

// ---------------------------------------------------------------
// PRESTAMO
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val usuarioId: Long,
    val nombre: String,
    val montoOriginal: Double,
    val meses: Int,
    val tipo: TipoPrestamo,     // reutiliza el enum de FinancialCalculator.kt
    val tasaAnual: Double,
    val fechaInicio: Long,      // timestamp en milisegundos
    val activarRecordatorio: Boolean,
    val diasAviso: Int
)

// ---------------------------------------------------------------
// PAGO_PRESTAMO (una fila por cada mes de la tabla de amortización)
// ---------------------------------------------------------------

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
    val id: Long = 0,
    val prestamoId: Long,
    val numeroPago: Int,
    val pagoMensual: Double,
    val interesPagado: Double,
    val capitalPagado: Double,
    val saldoRestante: Double,
    val pagado: Boolean
)
