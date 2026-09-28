package com.app.organigasto.data.local.dao

import androidx.room.*
import com.app.organigasto.data.local.entity.*
import com.app.organigasto.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :usuarioId")
    suspend fun obtenerPorId(usuarioId: Int): Usuario?

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)
}

@Dao
interface CategoriaDao {
    @Insert
    suspend fun insertar(categoria: Categoria): Long

    @Query("SELECT * FROM categoria WHERE usuarioId = :usuarioId ORDER BY nombre")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Categoria>>

    @Query("SELECT * FROM categoria WHERE id = :categoriaId")
    suspend fun obtenerPorId(categoriaId: Int): Categoria?

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)
}

@Dao
interface PresupuestoPeriodoDao {
    @Insert
    suspend fun insertar(presupuesto: PresupuestoPeriodo): Long

    @Query("SELECT * FROM presupuesto_periodo WHERE categoriaId = :categoriaId AND :fecha BETWEEN fechaInicio AND fechaFin LIMIT 1")
    suspend fun obtenerPeriodoActivo(categoriaId: Int, fecha: Long): PresupuestoPeriodo?

    @Query("SELECT * FROM presupuesto_periodo WHERE categoriaId = :categoriaId ORDER BY fechaInicio DESC")
    fun observarPorCategoria(categoriaId: Int): Flow<List<PresupuestoPeriodo>>

    @Update
    suspend fun actualizar(presupuesto: PresupuestoPeriodo)

    @Delete
    suspend fun eliminar(presupuesto: PresupuestoPeriodo)
}

@Dao
interface GastoDao {
    @Insert
    suspend fun insertar(gasto: Gasto): Long

    @Query("SELECT * FROM gasto WHERE categoriaId = :categoriaId ORDER BY fecha DESC")
    fun observarPorCategoria(categoriaId: Int): Flow<List<Gasto>>

    @Query("SELECT * FROM gasto WHERE categoriaId = :categoriaId AND fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun observarPorCategoriaYPeriodo(categoriaId: Int, desde: String, hasta: String): Flow<List<Gasto>>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gasto WHERE categoriaId = :categoriaId AND fecha BETWEEN :desde AND :hasta")
    suspend fun sumaPorCategoriaYPeriodo(categoriaId: Int, desde: String, hasta: String): Double

    @Query("SELECT * FROM gasto WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Gasto>>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gasto WHERE usuarioId = :usuarioId AND fecha BETWEEN :desde AND :hasta")
    suspend fun sumaTotalPorUsuarioYPeriodo(usuarioId: Int, desde: String, hasta: String): Double

    @Update
    suspend fun actualizar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}

@Dao
interface SuscripcionDao {
    @Insert
    suspend fun insertar(suscripcion: Suscripcion): Long

    @Query("SELECT * FROM suscripcion WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Suscripcion?

    @Query("SELECT * FROM suscripcion WHERE usuarioId = :usuarioId AND estado = :estado ORDER BY fechaProximoCobro")
    fun observarPorUsuario(usuarioId: Int, estado: String): Flow<List<Suscripcion>>

    @Query("""
        SELECT * FROM suscripcion
        WHERE activarRecordatorio = 1
        AND estado = :estadoActiva
        AND fechaProximoCobro >= :ahora
    """)
    suspend fun obtenerConRecordatorioPendiente(ahora: String, estadoActiva: String): List<Suscripcion>

    @Update
    suspend fun actualizar(suscripcion: Suscripcion)

    @Delete
    suspend fun eliminar(suscripcion: Suscripcion)
}

@Dao
interface CobroSuscripcionDao {
    @Insert
    suspend fun insertar(cobro: CobroSuscripcion): Long

    @Query("SELECT * FROM cobro_suscripcion WHERE suscripcionId = :suscripcionId ORDER BY fecha DESC")
    fun observarPorSuscripcion(suscripcionId: Int): Flow<List<CobroSuscripcion>>

    @Update
    suspend fun actualizar(cobro: CobroSuscripcion)
}

@Dao
interface PrestamoDao {
    @Insert
    suspend fun insertar(prestamo: Prestamo): Long

    @Query("SELECT * FROM prestamo WHERE usuarioId = :usuarioId ORDER BY fechaInicio DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Prestamo>>

    @Query("SELECT * FROM prestamo WHERE id = :prestamoId")
    suspend fun obtenerPorId(prestamoId: Int): Prestamo?

    @Delete
    suspend fun eliminar(prestamo: Prestamo)
}

@Dao
interface PagoPrestamoDao {
    @Insert
    suspend fun insertarTodos(pagos: List<PagoPrestamo>)

    @Query("SELECT * FROM pago_prestamo WHERE prestamoId = :prestamoId ORDER BY numeroPago")
    fun observarPorPrestamo(prestamoId: Int): Flow<List<PagoPrestamo>>

    @Query("SELECT * FROM pago_prestamo WHERE prestamoId = :prestamoId AND pagado = 0 ORDER BY numeroPago LIMIT 1")
    suspend fun obtenerSiguientePagoPendiente(prestamoId: Int): PagoPrestamo?

    @Update
    suspend fun actualizar(pago: PagoPrestamo)
}
