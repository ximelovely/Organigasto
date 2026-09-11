package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.CuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {
    @Query("SELECT * FROM cuentas")
    fun obtenerTodas(): Flow<List<CuentaEntity>>

    @Insert
    suspend fun insertar(cuenta: CuentaEntity)

    @Update
    suspend fun actualizar(cuenta: CuentaEntity)

    @Delete
    suspend fun eliminar(cuenta: CuentaEntity)

    @Query("UPDATE cuentas SET saldoActual = saldoActual + :monto WHERE id = :id")
    suspend fun ajustarSaldo(id: Long, monto: Double)
}
