package com.example.organigasto

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :usuarioId")
    suspend fun obtenerPorId(usuarioId: Long): Usuario?

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
    fun observarPorUsuario(usuarioId: Long): Flow<List<Categoria>>

    @Query("SELECT * FROM categoria WHERE id = :categoriaId")
    suspend fun obtenerPorId(categoriaId: Long): Categoria?

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)
}

@Dao
interface PresupuestoPeriodoDao {
    @Insert
    suspend fun insertar(presupuesto: PresupuestoPeriodo): Long

    /** Encuentra el periodo cuyo rango de fechas incluye la fecha dada. */
    @Query("SELECT * FROM presupuesto_periodo WHERE categoriaId = :categoriaId AND :fecha BETWEEN fechaInicio AND fechaFin LIMIT 1")
    suspend fun obtenerPeriodoActivo(categoriaId: Long, fecha: Long): PresupuestoPeriodo?

    @Query("SELECT * FROM presupuesto_periodo WHERE categoriaId = :categoriaId ORDER BY fechaInicio DESC")
    fun observarPorCategoria(categoriaId: Long): Flow<List<PresupuestoPeriodo>>

    @Update
    suspend fun actualizar(presupuesto: PresupuestoPeriodo)

    @Delete
    suspend fun eliminar(presupuesto: PresupuestoPeriodo)
}

@Dao
interface GastoDao {
    @Insert
    suspend fun insertar(gasto: Gasto): Long

    @Query("SELECT * FROM gasto WHERE categoriaId = :categoriaId AND fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun observarPorCategoriaYPeriodo(categoriaId: Long, desde: Long, hasta: Long): Flow<List<Gasto>>

    /** Este es el "montoGastado" que le vas a pasar a BudgetCalculator.calcularEstadoCategoria(). */
    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gasto WHERE categoriaId = :categoriaId AND fecha BETWEEN :desde AND :hasta")
    suspend fun sumaPorCategoriaYPeriodo(categoriaId: Long, desde: Long, hasta: Long): Double

    @Query("SELECT * FROM gasto WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Gasto>>

    /** Total global del usuario para el dashboard de Estadísticas - sin JOIN gracias al usuarioId denormalizado. */
    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gasto WHERE usuarioId = :usuarioId AND fecha BETWEEN :desde AND :hasta")
    suspend fun sumaTotalPorUsuarioYPeriodo(usuarioId: Long, desde: Long, hasta: Long): Double

    @Update
    suspend fun actualizar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}

@Dao
interface SuscripcionDao {
    @Insert
    suspend fun insertar(suscripcion: Suscripcion): Long

    @Query("SELECT * FROM suscripcion WHERE usuarioId = :usuarioId AND estado = :estado ORDER BY fechaProximoCobro")
    fun observarPorUsuario(usuarioId: Long, estado: EstadoSuscripcion): Flow<List<Suscripcion>>

    /** Suscripciones activas cuyo próximo cobro cae dentro de su ventana de aviso (diasAviso). */
    @Query("""
        SELECT * FROM suscripcion
        WHERE activarRecordatorio = 1
        AND estado = :estadoActiva
        AND fechaProximoCobro >= :ahora
        AND (fechaProximoCobro - :ahora) <= (diasAviso * 86400000)
    """)
    suspend fun obtenerConRecordatorioPendiente(ahora: Long, estadoActiva: EstadoSuscripcion): List<Suscripcion>

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
    fun observarPorSuscripcion(suscripcionId: Long): Flow<List<CobroSuscripcion>>

    @Update
    suspend fun actualizar(cobro: CobroSuscripcion)
}

@Dao
interface PrestamoDao {
    @Insert
    suspend fun insertar(prestamo: Prestamo): Long

    @Query("SELECT * FROM prestamo WHERE usuarioId = :usuarioId ORDER BY fechaInicio DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Prestamo>>

    @Query("SELECT * FROM prestamo WHERE id = :prestamoId")
    suspend fun obtenerPorId(prestamoId: Long): Prestamo?

    @Delete
    suspend fun eliminar(prestamo: Prestamo)
}

@Dao
interface PagoPrestamoDao {
    /** Inserta de un jalón toda la tabla de amortización que ya calculó LoanCalculator. */
    @Insert
    suspend fun insertarTodos(pagos: List<PagoPrestamo>)

    @Query("SELECT * FROM pago_prestamo WHERE prestamoId = :prestamoId ORDER BY numeroPago")
    fun observarPorPrestamo(prestamoId: Long): Flow<List<PagoPrestamo>>

    @Query("SELECT * FROM pago_prestamo WHERE prestamoId = :prestamoId AND pagado = 0 ORDER BY numeroPago LIMIT 1")
    suspend fun obtenerSiguientePagoPendiente(prestamoId: Long): PagoPrestamo?

    @Update
    suspend fun actualizar(pago: PagoPrestamo)
}
