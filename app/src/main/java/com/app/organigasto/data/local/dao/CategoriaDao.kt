package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Insert
    suspend fun insertar(categoria: CategoriaEntity): Long

    @Query("SELECT * FROM categorias")
    fun obtenerTodas(): Flow<List<CategoriaEntity>>
    
    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun contarCategorias(): Int

    @Update
    suspend fun actualizar(categoria: CategoriaEntity)

    @Query("UPDATE categorias SET presupuestoMensual = :presupuesto WHERE id = :id")
    suspend fun actualizarPresupuesto(id: Long, presupuesto: Double)

    @Delete
    suspend fun eliminar(categoria: CategoriaEntity)

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): CategoriaEntity?
}
