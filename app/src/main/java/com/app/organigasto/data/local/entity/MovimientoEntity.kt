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
            childColumns = ["categoriaId"]
        )
    ]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoMovimiento,
    val monto: Double,
    val categoriaId: Long,
    val cuenta: String,              // "Tarjeta debito BBVA", por ejemplo
    val fecha: LocalDate,
    val recurrencia: Recurrencia = Recurrencia.INDIVIDUAL,
    val nota: String? = null
)