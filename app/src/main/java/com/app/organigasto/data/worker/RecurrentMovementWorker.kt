package com.app.organigasto.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.organigasto.OrganigastoApp
import com.app.organigasto.domain.model.Recurrencia
import com.app.organigasto.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class RecurrentMovementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = (applicationContext as OrganigastoApp).database
        val movimientoDao = database.movimientoDao()
        val cuentaDao = database.cuentaDao()

        val hoy = LocalDate.now()
        val movimientos = movimientoDao.obtenerTodos().first()

        movimientos.filter { it.recurrencia != Recurrencia.INDIVIDUAL }.forEach { mov ->
            // Lógica simplificada: si la fecha del movimiento original + periodo es hoy, crear uno nuevo
            val proximaFecha = when (mov.recurrencia) {
                Recurrencia.SEMANAL -> mov.fecha.plusWeeks(1)
                Recurrencia.QUINCENAL -> mov.fecha.plusWeeks(2)
                Recurrencia.MENSUAL -> mov.fecha.plusMonths(1)
                else -> null
            }

            if (proximaFecha != null && proximaFecha.isEqual(hoy)) {
                // Verificar si ya se creó para esta fecha (opcional, para evitar duplicados)
                
                val nuevoMov = mov.copy(id = 0, fecha = hoy)
                movimientoDao.insertar(nuevoMov)
                
                // IMPORTANTE: Aquí necesitaríamos el cuentaId real. 
                // Por ahora, como el esquema actual guarda el nombre como String, 
                // tendríamos que buscar la cuenta por nombre o actualizar el esquema.
            }
        }

        return Result.success()
    }
}
