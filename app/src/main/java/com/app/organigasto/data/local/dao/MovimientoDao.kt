package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun obtenerPorRango(desde: LocalDate, hasta: LocalDate): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE categoriaId = :categoriaId ORDER BY fecha DESC")
    fun obtenerPorCategoria(categoriaId: Long): Flow<List<MovimientoEntity>>

    @Query("""
        SELECT SUM(monto) FROM movimientos 
        WHERE tipo = 'GASTO' AND fecha BETWEEN :desde AND :hasta
    """)
    fun totalGastosEnRango(desde: LocalDate, hasta: LocalDate): Flow<Double?>

    @Query("""
        SELECT SUM(monto) FROM movimientos 
        WHERE tipo = 'INGRESO' AND fecha BETWEEN :desde AND :hasta
    """)
    fun totalIngresosEnRango(desde: LocalDate, hasta: LocalDate): Flow<Double?>
}