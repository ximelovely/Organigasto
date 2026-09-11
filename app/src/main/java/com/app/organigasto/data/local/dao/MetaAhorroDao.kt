package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.MetaAhorroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaAhorroDao {
    @Query("SELECT * FROM metas_ahorro WHERE activa = 1")
    fun obtenerActivas(): Flow<List<MetaAhorroEntity>>

    @Insert
    suspend fun insertar(meta: MetaAhorroEntity)

    @Update
    suspend fun actualizar(meta: MetaAhorroEntity)

    @Query("UPDATE metas_ahorro SET montoActual = montoActual + :monto WHERE id = :id")
    suspend fun abonarAMeta(id: Long, monto: Double)
}
