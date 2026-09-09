package com.app.organigasto

import android.app.Application
import androidx.room.Room
import com.app.organigasto.data.local.AppDatabase
import com.app.organigasto.data.local.entity.CategoriaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
        ).build()

        prepopulateDatabase()
    }

    private fun prepopulateDatabase() {
        applicationScope.launch {
            val count = database.categoriaDao().contarCategorias()
            if (count == 0) {
                val categorias = listOf(
                    CategoriaEntity(nombre = "Comida", icono = "restaurant", colorHex = "#8B7BB8", presupuestoMensual = 500.0),
                    CategoriaEntity(nombre = "Transporte", icono = "directions_car", colorHex = "#8B7BB8", presupuestoMensual = 300.0),
                    CategoriaEntity(nombre = "Vivienda", icono = "home", colorHex = "#8B7BB8", presupuestoMensual = 1200.0),
                    CategoriaEntity(nombre = "Ocio", icono = "movie", colorHex = "#8B7BB8", presupuestoMensual = 200.0)
                )
                categorias.forEach { database.categoriaDao().insertar(it) }
            }
        }
    }
}
