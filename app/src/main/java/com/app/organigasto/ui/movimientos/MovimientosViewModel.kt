package com.app.organigasto.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.organigasto.data.local.dao.*
import com.app.organigasto.data.local.entity.*
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
    private val categoriaDao: CategoriaDao,
    private val cuentaDao: CuentaDao,
    private val metaAhorroDao: MetaAhorroDao,
    private val suscripcionDao: SuscripcionDao,
    private val deudaDao: DeudaDao
) : ViewModel() {

    private val _filtroTiempo = MutableStateFlow("Mensual")
    val filtroTiempo: StateFlow<String> = _filtroTiempo.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val categorias: StateFlow<List<CategoriaEntity>> = categoriaDao
        .obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cuentas: StateFlow<List<CuentaEntity>> = cuentaDao
        .obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metasAhorro: StateFlow<List<MetaAhorroEntity>> = metaAhorroDao
        .obtenerActivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suscripciones: StateFlow<List<SuscripcionEntity>> = suscripcionDao
        .obtenerActivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deudas: StateFlow<List<DeudaEntity>> = deudaDao
        .obtenerPendientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movimientosFiltrados: StateFlow<List<MovimientoEntity>> = combine(
        _filtroTiempo,
        _busqueda
    ) { filtro, query ->
        val hoy = LocalDate.now()
        val lista = when (filtro) {
            "Semanal" -> {
                val inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val fin = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                movimientoDao.obtenerPorRango(inicio, fin).first()
            }
            "Mensual" -> {
                val inicio = hoy.with(TemporalAdjusters.firstDayOfMonth())
                val fin = hoy.with(TemporalAdjusters.lastDayOfMonth())
                movimientoDao.obtenerPorRango(inicio, fin).first()
            }
            "Anual" -> {
                val inicio = hoy.with(TemporalAdjusters.firstDayOfYear())
                val fin = hoy.with(TemporalAdjusters.lastDayOfYear())
                movimientoDao.obtenerPorRango(inicio, fin).first()
            }
            else -> movimientoDao.obtenerTodos().first()
        }
        
        if (query.isBlank()) lista
        else lista.filter { 
            it.nota?.contains(query, ignoreCase = true) == true || 
            it.cuenta.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movimientosPorDia: StateFlow<Map<LocalDate, List<MovimientoEntity>>> = movimientoDao
        .obtenerTodos()
        .map { lista -> lista.groupBy { it.fecha } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    fun buscar(query: String) {
        _busqueda.value = query
    }

    fun agregarMovimiento(
        tipo: TipoMovimiento,
        monto: Double,
        categoriaId: Long?,
        cuentaId: Long,
        cuentaNombre: String,
        fecha: LocalDate,
        recurrencia: Recurrencia,
        nota: String? = null,
        moneda: String = "MXN",
        montoOriginal: Double = 0.0,
        tasaCambio: Double = 1.0
    ) {
        viewModelScope.launch {
            val nuevo = MovimientoEntity(
                tipo = tipo,
                monto = monto,
                categoriaId = categoriaId,
                cuentaId = cuentaId,
                cuenta = cuentaNombre,
                fecha = fecha,
                recurrencia = recurrencia,
                nota = nota,
                moneda = moneda,
                montoOriginal = montoOriginal,
                tasaCambio = tasaCambio
            )
            movimientoDao.insertar(nuevo)
            
            // Ajustar el saldo de la cuenta
            val ajuste = if (tipo == TipoMovimiento.INGRESO) monto else -monto
            cuentaDao.ajustarSaldo(cuentaId, ajuste)
        }
    }

    fun transferir(
        monto: Double,
        cuentaOrigenId: Long,
        cuentaOrigenNombre: String,
        cuentaDestinoId: Long,
        cuentaDestinoNombre: String,
        fecha: LocalDate,
        nota: String? = null
    ) {
        viewModelScope.launch {
            // 1. Registro de salida en cuenta origen
            val salida = MovimientoEntity(
                tipo = TipoMovimiento.TRANSFERENCIA,
                monto = monto,
                categoriaId = null,
                cuentaId = cuentaOrigenId,
                cuenta = cuentaOrigenNombre,
                fecha = fecha,
                nota = "Transferencia a $cuentaDestinoNombre${if (nota != null) ": $nota" else ""}"
            )
            movimientoDao.insertar(salida)
            cuentaDao.ajustarSaldo(cuentaOrigenId, -monto)

            // 2. Registro de entrada en cuenta destino
            val entrada = MovimientoEntity(
                tipo = TipoMovimiento.TRANSFERENCIA,
                monto = monto,
                categoriaId = null,
                cuentaId = cuentaDestinoId,
                cuenta = cuentaDestinoNombre,
                fecha = fecha,
                nota = "Transferencia desde $cuentaOrigenNombre${if (nota != null) ": $nota" else ""}"
            )
            movimientoDao.insertar(entrada)
            cuentaDao.ajustarSaldo(cuentaDestinoId, monto)
        }
    }

    fun obtenerTasaCambio(moneda: String): Double {
        return when (moneda) {
            "USD" -> 19.50 // Mock
            "EUR" -> 21.20 // Mock
            "GBP" -> 25.40 // Mock
            else -> 1.0
        }
    }

    fun agregarCuenta(nombre: String, tipo: String, saldo: Double) {
        viewModelScope.launch {
            cuentaDao.insertar(CuentaEntity(nombre = nombre, tipo = tipo, saldoInicial = saldo, saldoActual = saldo))
        }
    }

    fun eliminarMovimiento(movimiento: MovimientoEntity) {
        viewModelScope.launch {
            movimientoDao.eliminar(movimiento)
        }
    }

    fun actualizarPresupuesto(categoriaId: Long, monto: Double) {
        viewModelScope.launch {
            categoriaDao.actualizarPresupuesto(categoriaId, monto)
        }
    }

    fun agregarCategoria(nombre: String, icono: String, colorHex: String, presupuesto: Double = 0.0) {
        viewModelScope.launch {
            categoriaDao.insertar(CategoriaEntity(nombre = nombre, icono = icono, colorHex = colorHex, presupuestoMensual = presupuesto))
        }
    }

    fun eliminarCategoria(categoria: CategoriaEntity) {
        viewModelScope.launch {
            categoriaDao.eliminar(categoria)
        }
    }

    fun abonarAMeta(metaId: Long, monto: Double) {
        viewModelScope.launch {
            metaAhorroDao.abonarAMeta(metaId, monto)
        }
    }

    fun actualizarMeta(id: Long, montoObjetivo: Double) {
        viewModelScope.launch {
            val meta = metasAhorro.value.find { it.id == id }
            if (meta != null) {
                metaAhorroDao.actualizar(meta.copy(montoObjetivo = montoObjetivo))
            }
        }
    }

    fun agregarSuscripcion(nombre: String, monto: Double, proximoPago: LocalDate, categoriaId: Long) {
        viewModelScope.launch {
            suscripcionDao.insertar(SuscripcionEntity(nombre = nombre, monto = monto, proximoPago = proximoPago, categoriaId = categoriaId))
        }
    }

    fun liquidarDeuda(id: Long) {
        viewModelScope.launch {
            deudaDao.liquidar(id)
        }
    }

    fun agregarDeuda(persona: String, monto: Double, esPrestamo: Boolean, fechaLimite: LocalDate? = null) {
        viewModelScope.launch {
            deudaDao.insertar(DeudaEntity(persona = persona, monto = monto, esPrestamo = esPrestamo, fecha = LocalDate.now(), fechaLimite = fechaLimite))
        }
    }

    fun exportarDatosACsv(): String {
        val lista = movimientosFiltrados.value
        val sb = StringBuilder()
        sb.append("Fecha,Tipo,Monto,Cuenta,Nota\n")
        lista.forEach { mov ->
            sb.append("${mov.fecha},${mov.tipo},${mov.monto},${mov.cuenta},${mov.nota ?: ""}\n")
        }
        return sb.toString()
    }
}

class MovimientosViewModelFactory(
    private val movimientoDao: MovimientoDao,
    private val categoriaDao: CategoriaDao,
    private val cuentaDao: CuentaDao,
    private val metaAhorroDao: MetaAhorroDao,
    private val suscripcionDao: SuscripcionDao,
    private val deudaDao: DeudaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovimientosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovimientosViewModel(movimientoDao, categoriaDao, cuentaDao, metaAhorroDao, suscripcionDao, deudaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
