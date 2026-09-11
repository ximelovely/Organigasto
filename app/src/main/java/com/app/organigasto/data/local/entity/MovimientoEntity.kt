package com.app.organigasto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.organigasto.domain.model.Recurrencia
import com.app.organigasto.domain.model.TipoMovimiento
import java.time.LocalDate

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoMovimiento,
    val monto: Double,
    val categoriaId: Long? = null,
    val cuenta: String,
    val fecha: LocalDate,
    val recurrencia: Recurrencia = Recurrencia.INDIVIDUAL,
    val nota: String? = null,
    val imagenReciboPath: String? = null,
    val moneda: String = "MXN",        // Moneda original: "USD", "EUR", etc.
    val montoOriginal: Double = 0.0,  // Monto en la moneda original
    val tasaCambio: Double = 1.0      // Tasa aplicada para llegar al monto local
)
