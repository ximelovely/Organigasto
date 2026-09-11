package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.SuscripcionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuscripcionDao {
    @Query("SELECT * FROM suscripciones WHERE activa = 1")
    fun obtenerActivas(): Flow<List<SuscripcionEntity>>

    @Insert
    suspend fun insertar(suscripcion: SuscripcionEntity)

    @Update
    suspend fun actualizar(suscripcion: SuscripcionEntity)

    @Delete
    suspend fun eliminar(suscripcion: SuscripcionEntity)
}
