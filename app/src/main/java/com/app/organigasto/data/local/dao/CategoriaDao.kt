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
}
