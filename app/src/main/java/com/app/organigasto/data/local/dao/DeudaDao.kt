package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.DeudaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeudaDao {
    @Query("SELECT * FROM deudas WHERE liquidada = 0")
    fun obtenerPendientes(): Flow<List<DeudaEntity>>

    @Insert
    suspend fun insertar(deuda: DeudaEntity)

    @Update
    suspend fun actualizar(deuda: DeudaEntity)

    @Query("UPDATE deudas SET liquidada = 1 WHERE id = :id")
    suspend fun liquidar(id: Long)
}
