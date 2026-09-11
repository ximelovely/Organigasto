package com.app.organigasto

import android.app.Application
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.app.organigasto.data.local.AppDatabase
import com.app.organigasto.data.worker.RecurrentMovementWorker
import com.app.organigasto.data.local.entity.CategoriaEntity
import com.app.organigasto.data.local.entity.CuentaEntity
import com.app.organigasto.data.local.entity.MetaAhorroEntity
import com.app.organigasto.domain.model.TipoMovimiento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OrganigastoApp : Application() {
    lateinit var database: AppDatabase
        private set

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "organigasto_db"
        ).fallbackToDestructiveMigration().build()

        prepopulateDatabase()
        scheduleRecurrentWork()
    }

    private fun scheduleRecurrentWork() {
        val request = PeriodicWorkRequestBuilder<RecurrentMovementWorker>(1, TimeUnit.DAYS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecurrentMovements",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun prepopulateDatabase() {
        applicationScope.launch {
            val count = database.categoriaDao().contarCategorias()
            if (count == 0) {
                val categorias = listOf(
                    CategoriaEntity(nombre = "Comida", icono = "restaurant", colorHex = "#8B7BB8", presupuestoMensual = 500.0, tipo = TipoMovimiento.GASTO),
                    CategoriaEntity(nombre = "Transporte", icono = "directions_car", colorHex = "#8B7BB8", presupuestoMensual = 300.0, tipo = TipoMovimiento.GASTO),
                    CategoriaEntity(nombre = "Vivienda", icono = "home", colorHex = "#8B7BB8", presupuestoMensual = 1200.0, tipo = TipoMovimiento.GASTO),
                    CategoriaEntity(nombre = "Ocio", icono = "movie", colorHex = "#8B7BB8", presupuestoMensual = 200.0, tipo = TipoMovimiento.GASTO),
                    CategoriaEntity(nombre = "Sueldo", icono = "payments", colorHex = "#4CAF50", presupuestoMensual = 0.0, tipo = TipoMovimiento.INGRESO),
                    CategoriaEntity(nombre = "Venta", icono = "shopping_bag", colorHex = "#2196F3", presupuestoMensual = 0.0, tipo = TipoMovimiento.INGRESO),
                    CategoriaEntity(nombre = "Regalo", icono = "redeem", colorHex = "#FF9800", presupuestoMensual = 0.0, tipo = TipoMovimiento.INGRESO),
                    CategoriaEntity(nombre = "Otros", icono = "category", colorHex = "#9E9E9E", presupuestoMensual = 0.0, tipo = null)
                )
                categorias.forEach { database.categoriaDao().insertar(it) }
            }

            // Pre-poblar Cuentas
            val countCuentas = database.cuentaDao().obtenerTodas().first().size
            if (countCuentas == 0) {
                val cuentas = listOf(
                    CuentaEntity(nombre = "Efectivo", tipo = "Efectivo", saldoInicial = 0.0, saldoActual = 0.0, colorHex = "#4CAF50"),
                    CuentaEntity(nombre = "Tarjeta Débito", tipo = "Debito", saldoInicial = 0.0, saldoActual = 0.0, colorHex = "#2196F3")
                )
                cuentas.forEach { database.cuentaDao().insertar(it) }
            }

            // Pre-poblar una Meta de ejemplo
            val countMetas = database.metaAhorroDao().obtenerActivas().first().size
            if (countMetas == 0) {
                database.metaAhorroDao().insertar(
                    MetaAhorroEntity(nombre = "Fondo de Emergencia", montoObjetivo = 1000.0, montoActual = 0.0)
                )
            }
        }
    }
}
