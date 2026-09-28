package com.app.organigasto.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.organigasto.OrganigastoApp
import com.app.organigasto.data.local.entity.CobroSuscripcion
import com.app.organigasto.data.local.entity.Gasto
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class RecurrentMovementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = (applicationContext as OrganigastoApp).database
        val hoy = LocalDate.now()
        
        // Procesar Suscripciones
        val suscripciones = database.suscripcionDao().observarPorUsuario(1, "ACTIVA").first()

        suscripciones.forEach { sub ->
            try {
                val proximoPago = LocalDate.parse(sub.fechaProximoCobro)
                if (!hoy.isBefore(proximoPago)) {
                    // 1. Generar cobro en el historial de suscripción
                    database.cobroSuscripcionDao().insertar(
                        CobroSuscripcion(
                            suscripcionId = sub.id,
                            fecha = sub.fechaProximoCobro,
                            monto = sub.costo,
                            estado = "COBRADO"
                        )
                    )
                    
                    // 2. Generar Gasto real para el Organigasto
                    database.gastoDao().insertar(
                        Gasto(
                            usuarioId = sub.usuarioId,
                            categoriaId = sub.categoriaId ?: 0,
                            monto = sub.costo,
                            fecha = sub.fechaProximoCobro,
                            descripcion = "[Suscripción] ${sub.nombre}"
                        )
                    )

                    // 3. Programar la PRÓXIMA fecha
                    val nuevaFecha = when (sub.frecuencia) {
                        "MENSUAL" -> proximoPago.plusMonths(1)
                        "ANUAL" -> proximoPago.plusYears(1)
                        else -> proximoPago.plusMonths(1)
                    }.toString()
                    
                    database.suscripcionDao().actualizar(sub.copy(fechaProximoCobro = nuevaFecha))
                }
            } catch (e: Exception) {
                // Error al parsear fecha o procesar
            }
        }

        return Result.success()
    }
}
