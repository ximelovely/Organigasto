package com.app.organigasto

import android.app.Application
import androidx.room.Room
import com.app.organigasto.data.local.AppDatabase
import com.app.organigasto.data.local.entity.Categoria
import com.app.organigasto.data.local.entity.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
        ).fallbackToDestructiveMigration().build()

        prepopulateDatabase()
    }

    private fun prepopulateDatabase() {
        applicationScope.launch {
            // Aseguramos que exista al menos el usuario admin inicial
            val user = database.usuarioDao().obtenerPorId(1)
            if (user == null) {
                database.usuarioDao().insertar(
                    Usuario(id = 1, nombre = "Demo User", correo = "demo@organigasto.com", passwordHash = "demo", salt = "salt")
                )
            }
        }
    }
}
