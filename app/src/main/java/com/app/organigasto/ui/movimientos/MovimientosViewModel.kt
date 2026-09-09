package com.app.organigasto.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.organigasto.data.local.dao.MovimientoDao
import com.app.organigasto.data.local.dao.CategoriaDao
import com.app.organigasto.data.local.entity.MovimientoEntity
import com.app.organigasto.data.local.entity.CategoriaEntity
import com.app.organigasto.domain.model.TipoMovimiento
import com.app.organigasto.domain.model.Recurrencia
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

data class GastoCategoria(
    val nombre: String,
    val gastado: Double,
    val presupuesto: Double
)

class MovimientosViewModel(
    private val movimientoDao: MovimientoDao,
    private val categoriaDao: CategoriaDao
) : ViewModel() {

    private val _filtroTiempo = MutableStateFlow("Mensual")
    val filtroTiempo: StateFlow<String> = _filtroTiempo.asStateFlow()

    val categorias: StateFlow<List<CategoriaEntity>> = categoriaDao
        .obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movimientosFiltrados: StateFlow<List<MovimientoEntity>> = _filtroTiempo
        .flatMapLatest { filtro ->
            val hoy = LocalDate.now()
            when (filtro) {
                "Semanal" -> {
                    val inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val fin = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    movimientoDao.obtenerPorRango(inicio, fin)
                }
                "Mensual" -> {
                    val inicio = hoy.with(TemporalAdjusters.firstDayOfMonth())
                    val fin = hoy.with(TemporalAdjusters.lastDayOfMonth())
                    movimientoDao.obtenerPorRango(inicio, fin)
                }
                "Anual" -> {
                    val inicio = hoy.with(TemporalAdjusters.firstDayOfYear())
                    val fin = hoy.with(TemporalAdjusters.lastDayOfYear())
                    movimientoDao.obtenerPorRango(inicio, fin)
                }
                else -> movimientoDao.obtenerTodos()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balanceTotal: StateFlow<Double> = movimientosFiltrados.map { lista ->
        lista.sumOf { if (it.tipo == TipoMovimiento.INGRESO) it.monto else -it.monto }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gastosPorCategoria: StateFlow<List<GastoCategoria>> = combine(
        movimientosFiltrados,
        categorias
    ) { movimientos, cats ->
        cats.map { cat ->
            val gastado = movimientos
                .filter { it.categoriaId == cat.id && it.tipo == TipoMovimiento.GASTO }
                .sumOf { it.monto }
            GastoCategoria(
                nombre = cat.nombre,
                gastado = gastado,
                presupuesto = cat.presupuestoMensual
            )
        }.filter { it.presupuesto > 0 || it.gastado > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movimientosRecurrentes: StateFlow<List<MovimientoEntity>> = movimientoDao
        .obtenerTodos()
        .map { lista ->
            lista.filter { it.recurrencia != Recurrencia.INDIVIDUAL }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFiltro(filtro: String) {
        _filtroTiempo.value = filtro
    }

    fun agregarMovimiento(
        tipo: TipoMovimiento,
        monto: Double,
        categoriaId: Long,
        cuenta: String,
        fecha: LocalDate,
        recurrencia: Recurrencia,
        nota: String? = null
    ) {
        viewModelScope.launch {
            val nuevo = MovimientoEntity(
                tipo = tipo,
                monto = monto,
                categoriaId = categoriaId,
                cuenta = cuenta,
                fecha = fecha,
                recurrencia = recurrencia,
                nota = nota
            )
            movimientoDao.insertar(nuevo)
        }
    }

    fun eliminarMovimiento(movimiento: MovimientoEntity) {
        viewModelScope.launch {
            movimientoDao.eliminar(movimiento)
        }
    }
}

class MovimientosViewModelFactory(
    private val movimientoDao: MovimientoDao,
    private val categoriaDao: CategoriaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovimientosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovimientosViewModel(movimientoDao, categoriaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
