package com.app.organigasto.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.organigasto.OrganigastoApp
import com.app.organigasto.domain.model.Recurrencia
import com.app.organigasto.domain.model.TipoMovimiento
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
            // Si la fecha actual es igual o posterior a la fecha programada
            if (!hoy.isBefore(mov.fecha)) {
                // 1. Crear el movimiento real (transacción puntual)
                val transaccionReal = mov.copy(
                    id = 0, 
                    fecha = mov.fecha, 
                    recurrencia = Recurrencia.INDIVIDUAL,
                    nota = "[Auto] ${mov.nota ?: "Recurrente"}"
                )
                movimientoDao.insertar(transaccionReal)

                // 2. Actualizar el saldo de la cuenta
                val ajuste = if (mov.tipo == TipoMovimiento.INGRESO) mov.monto else -mov.monto
                cuentaDao.ajustarSaldo(mov.cuentaId, ajuste)

                // 3. Programar la PRÓXIMA fecha en el movimiento "maestro"
                val proximaFecha = when (mov.recurrencia) {
                    Recurrencia.SEMANAL -> mov.fecha.plusWeeks(1)
                    Recurrencia.QUINCENAL -> mov.fecha.plusWeeks(2)
                    Recurrencia.MENSUAL -> mov.fecha.plusMonths(1)
                    else -> mov.fecha
                }
                movimientoDao.actualizar(mov.copy(fecha = proximaFecha))
            }
        }

        return Result.success()
    }
}
