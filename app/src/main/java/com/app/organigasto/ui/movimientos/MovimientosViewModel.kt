package com.app.organigasto.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.organigasto.data.local.dao.*
import com.app.organigasto.data.local.entity.*
import com.app.organigasto.domain.model.*
import com.app.organigasto.domain.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class MetaAhorro(
    val id: Int,
    val nombre: String,
    val montoObjetivo: Double,
    val montoActual: Double,
    val porcentaje: Double,
    val faltante: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientosViewModel(
    private val categoriaDao: CategoriaDao,
    private val presupuestoPeriodoDao: PresupuestoPeriodoDao,
    private val gastoDao: GastoDao,
    private val suscripcionDao: SuscripcionDao,
    private val prestamoDao: PrestamoDao,
    private val pagoPrestamoDao: PagoPrestamoDao,
    private val usuarioDao: UsuarioDao,
    private val usuarioId: Int
) : ViewModel() {

    val currentUserId: Int = if (usuarioId > 0) usuarioId else 1

    private suspend fun ensureUserExists() {
        try {
            if (usuarioDao.obtenerPorId(currentUserId) == null) {
                usuarioDao.insertar(
                    Usuario(
                        id = currentUserId,
                        nombre = "Usuario OrganiGasto",
                        correo = "demo@organigasto.com",
                        passwordHash = "demo",
                        salt = "salt"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _filtroTiempo = MutableStateFlow("Mensual")
    val filtroTiempo: StateFlow<String> = _filtroTiempo.asStateFlow()

    val categorias: StateFlow<List<Categoria>> = categoriaDao
        .observarPorUsuario(currentUserId)
        .map { list ->
            list.filter {
                it.nombre != "Saldo Inicial" &&
                !it.nombre.contains("Sueldo", true) &&
                !it.nombre.contains("Ingreso", true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gastos: StateFlow<List<Gasto>> = gastoDao
        .observarPorUsuario(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suscripciones: StateFlow<List<Suscripcion>> = suscripcionDao
        .observarPorUsuario(currentUserId, "ACTIVA")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suscripcionesPausadas: StateFlow<List<Suscripcion>> = suscripcionDao
        .observarPorUsuario(currentUserId, "PAUSADA")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePausaSuscripcion(suscripcion: Suscripcion) {
        viewModelScope.launch {
            try {
                val nuevoEstado = if (suscripcion.estado == "ACTIVA") "PAUSADA" else "ACTIVA"
                suscripcionDao.actualizar(suscripcion.copy(estado = nuevoEstado))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val prestamos: StateFlow<List<Prestamo>> = prestamoDao
        .observarPorUsuario(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtramos los préstamos reales (sin prefijo [AHORRO])
    val deudasReales: StateFlow<List<Prestamo>> = prestamos.map { list ->
        list.filter { !it.nombre.startsWith("[AHORRO]") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtramos las metas de ahorro y calculamos su progreso
    val metasAhorro: StateFlow<List<MetaAhorro>> = prestamos.flatMapLatest { list ->
        val savings = list.filter { it.nombre.startsWith("[AHORRO]") }
        if (savings.isEmpty()) return@flatMapLatest flowOf(emptyList())

        val flows = savings.map { s ->
            pagoPrestamoDao.observarPorPrestamo(s.id).map { pagos ->
                val totalAhorrado = pagos.filter { it.pagado }.sumOf { it.pagoMensual }
                val nombreLimpio = s.nombre.removePrefix("[AHORRO] ").trim()
                val porcentaje = if (s.montoOriginal > 0) (totalAhorrado / s.montoOriginal).coerceIn(0.0, 1.0) else 0.0
                MetaAhorro(
                    id = s.id,
                    nombre = if (nombreLimpio.isEmpty()) "Sin nombre" else nombreLimpio,
                    montoObjetivo = s.montoOriginal,
                    montoActual = totalAhorrado,
                    porcentaje = porcentaje,
                    faltante = (s.montoOriginal - totalAhorrado).coerceAtLeast(0.0)
                )
            }
        }
        combine(flows) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presupuestos: StateFlow<List<PresupuestoPeriodo>> = categoriaDao
        .observarPorUsuario(currentUserId)
        .flatMapLatest { cats ->
            if (cats.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val budgetFlows = cats.map { cat ->
                presupuestoPeriodoDao.observarPorCategoria(cat.id)
            }
            combine(budgetFlows) { it.toList().flatten() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<Map<LocalDate, List<CalendarEvent>>> = combine(
        gastos,
        suscripciones,
        presupuestos,
        categoriaDao.observarPorUsuario(currentUserId)
    ) { allGastos, subs, allBudgets, cats ->
        val events = mutableListOf<CalendarEvent>()

        // Gastos
        allGastos.forEach { g ->
            try { events.add(CalendarEvent(g.descripcion, g.monto, "GASTO", LocalDate.parse(g.fecha))) } catch (e: Exception) {}
        }

        // Suscripciones
        subs.forEach { s ->
            try { events.add(CalendarEvent(s.nombre, s.costo, "SUSCRIPCION", LocalDate.parse(s.fechaProximoCobro))) } catch (e: Exception) {}
        }

        // Ingresos (Sueldos)
        allBudgets.forEach { b ->
            val cat = cats.find { it.id == b.categoriaId }
            val isIngreso = cat?.nombre?.contains("Sueldo", true) == true ||
                    cat?.nombre?.contains("Ingreso", true) == true ||
                    cat?.nombre?.contains("Saldo Inicial", true) == true

            if (isIngreso && b.fechaInicio > 0) {
                val date = Instant.ofEpochMilli(b.fechaInicio).atZone(ZoneId.systemDefault()).toLocalDate()
                events.add(CalendarEvent(cat?.nombre ?: "Cobro Sueldo", b.montoAsignado, "INGRESO", date))
            }
        }

        events.groupBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Estado de presupuestos por categoría (Sobres)
    val presupuestoStatus: StateFlow<List<CategoryBudgetStatus>> = combine(
        categorias,
        gastos,
        presupuestos,
        _filtroTiempo
    ) { cats, allGastos, allBudgets, filtro ->
        val hoy = LocalDate.now()
        val pattern = if (filtro == "Mensual") "yyyy-MM" else "yyyy-MM-dd"
        val prefijoFecha = hoy.format(DateTimeFormatter.ofPattern(pattern))

        cats.filter {
            it.nombre != "Saldo Inicial"
        }.map { cat ->
            val b = allBudgets.find {
                it.categoriaId == cat.id &&
                        System.currentTimeMillis() in it.fechaInicio..it.fechaFin
            }

            val totalGastado = allGastos
                .filter { it.categoriaId == cat.id && it.fecha.startsWith(prefijoFecha) }
                .sumOf { it.monto }

            BudgetCalculator.calcularEstadoCategoria(
                categoriaId = cat.id,
                nombre = cat.nombre,
                montoAsignado = b?.montoAsignado ?: 0.0,
                montoGastado = totalGastado,
                colorHex = cat.colorHex,
                iconoNombre = cat.iconoNombre,
                iconoUri = cat.iconoUri
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Proyección de saldo final de mes
    val proyeccionSaldo: StateFlow<ProyeccionSaldo> = combine(
        combine(presupuestoStatus, suscripciones, prestamos) { a, b, c -> Triple(a, b, c) },
        combine(presupuestos, gastos, categoriaDao.observarPorUsuario(currentUserId)) { a, b, c -> Triple(a, b, c) }
    ) { group1, group2 ->
        val budgets = group1.first
        val subs = group1.second
        val loans = group1.third

        val allBudgets = group2.first
        val allRealGastos = group2.second
        val allCatsUnfiltered = group2.third

        val today = LocalDate.now()

        // 1. Identificar Saldo Inicial (dinero actual real)
        val initialCat = allCatsUnfiltered.find { it.nombre == "Saldo Inicial" }
        val saldoInicialMonto = if (initialCat != null) {
            allBudgets.find { it.categoriaId == initialCat.id }?.montoAsignado ?: 0.0
        } else 0.0

        // 2. Identificar Sueldos / Ingresos recurrentes
        val sueldoCatIds = allCatsUnfiltered.filter {
            (it.nombre.contains("Sueldo", true) || it.nombre.contains("Ingreso", true)) &&
                    it.nombre != "Saldo Inicial"
        }.map { it.id }

        val userSalaries = allBudgets.filter { it.categoriaId in sueldoCatIds }

        var ingresosRealizados = 0.0
        val ingresosPendientesList = mutableListOf<Double>()

        userSalaries.forEach { sal ->
            val payDate = if (sal.fechaInicio > 0) {
                try {
                    Instant.ofEpochMilli(sal.fechaInicio).atZone(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) { today }
            } else today

            if (payDate.isAfter(today)) {
                ingresosPendientesList.add(sal.montoAsignado)
            } else {
                ingresosRealizados += sal.montoAsignado
            }
        }

        // 3. Gastos Reales realizados hasta hoy
        val gastosReales = allRealGastos.sumOf { it.monto }

        // 4. Cuotas de Préstamos / Deudas (no ahorros)
        var cuotasPagadas = 0.0
        val cuotasPendientesList = mutableListOf<Double>()

        loans.filter { !it.nombre.startsWith("[AHORRO]") }.forEach { loan ->
            val tipo = if (loan.tasaAnual > 0) TipoPrestamo.CON_INTERES else TipoPrestamo.SIN_INTERESES
            val pagoMensual = try {
                LoanCalculator.calcularPrestamo(loan.montoOriginal, loan.meses.coerceAtLeast(1), tipo, loan.tasaAnual).pagoMensual
            } catch (e: Exception) {
                if (loan.meses > 0) loan.montoOriginal / loan.meses else 0.0
            }

            val dueDay = (loan.diaPagoMensual ?: 15).coerceIn(1, 31)

            if (today.dayOfMonth >= dueDay) {
                cuotasPagadas += pagoMensual
            } else {
                cuotasPendientesList.add(pagoMensual)
            }
        }

        // 5. Suscripciones
        var suscripcionesPagadas = 0.0
        val suscripcionesPendientesList = mutableListOf<Double>()

        subs.forEach { sub ->
            val subDay = try {
                LocalDate.parse(sub.fechaProximoCobro).dayOfMonth
            } catch (e: Exception) { 1 }

            if (today.dayOfMonth >= subDay) {
                suscripcionesPagadas += sub.costo
            } else {
                suscripcionesPendientesList.add(sub.costo)
            }
        }

        // 6. Gastos proyectados restantes en los sobres
        val gastosProy = budgets.map { it.restante.coerceAtLeast(0.0) }

        CashFlowCalculator.calcularProyeccion(
            saldoInicial = saldoInicialMonto,
            ingresosRealizados = ingresosRealizados,
            gastosRealizados = gastosReales,
            cuotasPagadas = cuotasPagadas,
            suscripcionesPagadas = suscripcionesPagadas,
            ingresosPendientes = ingresosPendientesList,
            gastosPresupuestados = gastosProy,
            cuotasPrestamosPendientes = cuotasPendientesList,
            suscripcionesPendientes = suscripcionesPendientesList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProyeccionSaldo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

    val categoryForecasts: StateFlow<List<CategoryForecast>> = combine(
        presupuestoStatus,
        presupuestos
    ) { statusList, salaries ->
        val hoy = LocalDate.now()
        val diasTranscurridos = hoy.dayOfMonth

        val proximoSueldo = salaries
            .map { Instant.ofEpochMilli(it.fechaInicio).atZone(ZoneId.systemDefault()).toLocalDate() }
            .filter { it.isAfter(hoy) }
            .minByOrNull { it.toEpochDay() }
            ?: hoy.withDayOfMonth(hoy.lengthOfMonth())

        val diasHastaSueldo = (proximoSueldo.toEpochDay() - hoy.toEpochDay()).toInt().coerceAtLeast(1)

        statusList.map { status ->
            ForecastingCalculator.predecirAgotamiento(status, diasTranscurridos, diasHastaSueldo)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deudaEstrategia: StateFlow<List<Prestamo>> = deudasReales.map { list ->
        list.sortedByDescending { it.tasaAnual }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reporteVolatilidad: StateFlow<ReporteVolatilidad> = combine(
        proyeccionSaldo,
        calendarEvents
    ) { proy, eventsMap ->
        val eventosList = eventsMap.values.flatten()
        VolatilityCalculator.analizarEstabilidad(proy.saldoActual, eventosList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReporteVolatilidad(0, 0.0, 0))

    val diasHastaProximoSueldo: StateFlow<Int> = combine(
        presupuestos,
        categoriaDao.observarPorUsuario(currentUserId)
    ) { allBudgets, cats ->
        val hoy = LocalDate.now()
        val sueldoCat = cats.find { it.nombre.contains("Sueldo", true) || it.nombre.contains("Ingreso", true) }

        val proximoSueldoDate = if (sueldoCat != null) {
            val salBudgets = allBudgets.filter { it.categoriaId == sueldoCat.id }
            salBudgets
                .map { Instant.ofEpochMilli(it.fechaInicio).atZone(ZoneId.systemDefault()).toLocalDate() }
                .filter { !it.isBefore(hoy) }
                .minByOrNull { it.toEpochDay() }
        } else null

        if (proximoSueldoDate != null) {
            (proximoSueldoDate.toEpochDay() - hoy.toEpochDay()).toInt().coerceAtLeast(1)
        } else {
            (hoy.lengthOfMonth() - hoy.dayOfMonth + 1).coerceAtLeast(1)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val gastoDiarioStatus: StateFlow<Map<Int, Double>> = combine(
        presupuestoStatus,
        presupuestos
    ) { statusList, allBudgets ->
        val hoy = LocalDate.now()

        statusList.associate { status ->
            val b = allBudgets.find { it.categoriaId == status.categoriaId && System.currentTimeMillis() in it.fechaInicio..it.fechaFin }
            val diasRestantes = if (b != null) {
                val fin = Instant.ofEpochMilli(b.fechaFin).atZone(ZoneId.systemDefault()).toLocalDate()
                (fin.toEpochDay() - hoy.toEpochDay()).toInt().coerceAtLeast(1)
            } else 30

            status.categoriaId to BudgetCalculator.calcularGastoDiarioPermitido(status, diasRestantes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun buscar(query: String) {
        _busqueda.value = query
    }

    fun observarPagosPrestamo(prestamoId: Int): Flow<List<PagoPrestamo>> {
        return pagoPrestamoDao.observarPorPrestamo(prestamoId)
    }

    fun observarGastosPorCategoria(categoriaId: Int): Flow<List<Gasto>> {
        return gastoDao.observarPorCategoria(categoriaId)
    }

    fun setFiltro(filtro: String) {
        _filtroTiempo.value = filtro
    }

    fun agregarGasto(
        monto: Double,
        categoriaId: Int,
        descripcion: String,
        cuentaPago: String = "Débito"
    ) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                val hoy = LocalDate.now().toString()
                gastoDao.insertar(
                    Gasto(
                        usuarioId = currentUserId,
                        categoriaId = categoriaId,
                        monto = monto,
                        fecha = hoy,
                        descripcion = descripcion,
                        cuentaPago = cuentaPago
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarGasto(gasto: Gasto) {
        viewModelScope.launch {
            try {
                gastoDao.actualizar(gasto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            try {
                gastoDao.eliminar(gasto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarCategoriaConPresupuesto(
        nombre: String,
        presupuesto: Double,
        tipoPeriodo: TipoPeriodo = TipoPeriodo.MENSUAL,
        colorHex: String? = null,
        iconoNombre: String? = null,
        iconoUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                val id = categoriaDao.insertar(
                    Categoria(
                        usuarioId = currentUserId,
                        nombre = nombre,
                        colorHex = colorHex,
                        iconoNombre = iconoNombre,
                        iconoUri = iconoUri
                    )
                ).toInt()
                if (presupuesto > 0) {
                    val hoy = System.currentTimeMillis()
                    val fin = hoy + (30L * 24 * 60 * 60 * 1000)
                    presupuestoPeriodoDao.insertar(
                        PresupuestoPeriodo(
                            categoriaId = id,
                            tipoPeriodo = tipoPeriodo.name,
                            fechaInicio = hoy,
                            fechaFin = fin,
                            montoAsignado = presupuesto
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarCategoria(
        id: Int,
        nombre: String,
        colorHex: String? = null,
        iconoNombre: String? = null,
        iconoUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                categoriaDao.obtenerPorId(id)?.let {
                    categoriaDao.actualizar(
                        it.copy(
                            nombre = nombre,
                            colorHex = colorHex,
                            iconoNombre = iconoNombre,
                            iconoUri = iconoUri
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarCategoria(id: Int) {
        viewModelScope.launch {
            try {
                categoriaDao.obtenerPorId(id)?.let {
                    categoriaDao.eliminar(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun registrarPrestamo(
        nombre: String,
        monto: Double,
        meses: Int,
        tasa: Double,
        categoriaId: Int? = null,
        fechaInicio: String = LocalDate.now().toString(),
        diaPagoMensual: Int = 15,
        tipoCompromiso: String = "DEUDA",
        fechaVencimiento: String? = null
    ) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                val safeMeses = meses.coerceAtLeast(1)
                val tipo = if (tasa > 0) TipoPrestamo.CON_INTERES else TipoPrestamo.SIN_INTERESES
                val resumen = LoanCalculator.calcularPrestamo(monto, safeMeses, tipo, tasa)

                val tagNombre = if (tipoCompromiso == "PRÉSTAMO") "[PRÉSTAMO] $nombre" else "[DEUDA] $nombre"

                val prestamoId = prestamoDao.insertar(
                    Prestamo(
                        usuarioId = currentUserId,
                        nombre = tagNombre,
                        montoOriginal = monto,
                        meses = safeMeses,
                        tipo = tipo.name,
                        tasaAnual = tasa,
                        fechaInicio = fechaInicio,
                        activarRecordatorio = true,
                        diasAviso = 1,
                        categoriaId = categoriaId,
                        diaPagoMensual = diaPagoMensual,
                        fechaVencimiento = fechaVencimiento
                    )
                ).toInt()

                val pagos = resumen.tablaAmortizacion.map { fila ->
                    PagoPrestamo(
                        prestamoId = prestamoId,
                        numeroPago = fila.numeroPago,
                        pagoMensual = fila.pagoMensual,
                        interesPagado = fila.interesPagado,
                        capitalPagado = fila.capitalPagado,
                        saldoRestante = fila.saldoRestante,
                        pagado = false
                    )
                }
                pagoPrestamoDao.insertarTodos(pagos)

                if (categoriaId != null && categoriaId > 0) {
                    configurarPresupuesto(
                        categoriaId = categoriaId,
                        monto = resumen.pagoMensual,
                        tipo = TipoPeriodo.MENSUAL
                    )

                    // Insertamos el gasto de la cuota en el sobre seleccionado
                    gastoDao.insertar(
                        Gasto(
                            usuarioId = currentUserId,
                            categoriaId = categoriaId,
                            monto = resumen.pagoMensual,
                            fecha = LocalDate.now().toString(),
                            descripcion = "[Cuota Deuda] $nombre",
                            cuentaPago = "Crédito"
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarSuscripcion(
        nombre: String,
        costo: Double,
        categoriaId: Int? = null,
        frecuencia: String = "MENSUAL",
        fechaProximoCobro: String = LocalDate.now().toString(),
        esAutomatica: Boolean = true,
        activarRecordatorio: Boolean = true,
        diasAviso: Int = 1
    ) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                suscripcionDao.insertar(
                    Suscripcion(
                        usuarioId = currentUserId,
                        categoriaId = categoriaId,
                        nombre = nombre,
                        costo = costo,
                        fechaProximoCobro = fechaProximoCobro,
                        esAutomatica = esAutomatica,
                        frecuencia = frecuencia,
                        estado = "ACTIVA",
                        activarRecordatorio = activarRecordatorio,
                        diasAviso = diasAviso
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarSuscripcion(id: Int) {
        viewModelScope.launch {
            try {
                suscripcionDao.obtenerPorId(id)?.let {
                    suscripcionDao.eliminar(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarSuscripcion(suscripcion: Suscripcion) {
        viewModelScope.launch {
            try {
                suscripcionDao.actualizar(suscripcion)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun inicializarSueldoTutorial(
        monto: Double,
        frecuencia: TipoPeriodo = TipoPeriodo.MENSUAL,
        diaPago: Int = 15,
        esquemaQuincenal: String = "15_30",
        diaSemana: Int = 5
    ) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                val cats = categoriaDao.observarPorUsuario(currentUserId).first()
                var sueldoCat = cats.find {
                    it.nombre.contains("Sueldo", true) ||
                            it.nombre.contains("Ingreso", true)
                }

                if (sueldoCat == null) {
                    val newId = categoriaDao.insertar(
                        Categoria(
                            usuarioId = currentUserId,
                            nombre = "Sueldos (Ingresos)",
                            colorHex = "#43A047",
                            iconoNombre = "ahorro"
                        )
                    ).toInt()
                    sueldoCat = Categoria(id = newId, usuarioId = currentUserId, nombre = "Sueldos (Ingresos)", colorHex = "#43A047", iconoNombre = "ahorro")
                }

                // Limpiar registros de sueldos previos para evitar acumulación no deseada
                val oldBudgets = presupuestoPeriodoDao.observarPorCategoria(sueldoCat.id).first()
                oldBudgets.forEach { presupuestoPeriodoDao.eliminar(it) }

                if (monto > 0) {
                    val hoy = LocalDate.now()
                    val proximoPagoDate = when (frecuencia) {
                        TipoPeriodo.SEMANAL -> {
                            var target = hoy
                            while (target.dayOfWeek.value != diaSemana) {
                                target = target.plusDays(1)
                            }
                            if (target == hoy) target.plusWeeks(1) else target
                        }
                        TipoPeriodo.QUINCENAL -> {
                            val (p1, p2) = if (esquemaQuincenal == "1_16") 1 to 16 else 15 to 30
                            when {
                                hoy.dayOfMonth < p1 -> hoy.withDayOfMonth(p1)
                                hoy.dayOfMonth < p2 -> hoy.withDayOfMonth(p2.coerceAtMost(hoy.lengthOfMonth()))
                                else -> hoy.plusMonths(1).withDayOfMonth(p1)
                            }
                        }
                        TipoPeriodo.MENSUAL -> {
                            val safeDay = diaPago.coerceIn(1, hoy.lengthOfMonth())
                            if (hoy.dayOfMonth < safeDay) {
                                hoy.withDayOfMonth(safeDay)
                            } else {
                                val nextM = hoy.plusMonths(1)
                                nextM.withDayOfMonth(safeDay.coerceIn(1, nextM.lengthOfMonth()))
                            }
                        }
                    }

                    val inicioEpoch = proximoPagoDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                    val finEpoch = proximoPagoDate.plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

                    presupuestoPeriodoDao.insertar(
                        PresupuestoPeriodo(
                            categoriaId = sueldoCat.id,
                            tipoPeriodo = frecuencia.name,
                            fechaInicio = inicioEpoch,
                            fechaFin = finEpoch,
                            montoAsignado = monto
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun registrarMetaAhorro(nombre: String, montoObjetivo: Double) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                prestamoDao.insertar(
                    Prestamo(
                        usuarioId = currentUserId,
                        nombre = "[AHORRO] $nombre",
                        montoOriginal = montoObjetivo,
                        meses = 0,
                        tipo = "AHORRO",
                        tasaAnual = 0.0,
                        fechaInicio = LocalDate.now().toString(),
                        activarRecordatorio = false,
                        diasAviso = 0
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun abonarAMeta(metaId: Int, monto: Double) {
        viewModelScope.launch {
            try {
                pagoPrestamoDao.insertarTodos(
                    listOf(
                        PagoPrestamo(
                            prestamoId = metaId,
                            numeroPago = 0,
                            pagoMensual = monto,
                            interesPagado = 0.0,
                            capitalPagado = monto,
                            saldoRestante = 0.0,
                            pagado = true
                        )
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun parsearTextoGasto(texto: String): Pair<Double, String>? {
        val cleanText = texto.replace(",", "")
        val montoRegex = Regex("""\$?\s*(\d+(\.\d{1,2})?)""")
        val match = montoRegex.find(cleanText)

        return if (match != null) {
            val monto = match.groupValues[1].toDoubleOrNull() ?: return null
            if (monto <= 0) return null
            val rawDesc = texto.replace(Regex("""\$?\s*[\d,]+(\.\d{1,2})?"""), "").trim()
            val desc = if (rawDesc.isBlank()) "Gasto Rápido" else rawDesc.take(30)
            monto to desc
        } else null
    }

    fun establecerSaldoInicial(monto: Double) {
        viewModelScope.launch {
            try {
                ensureUserExists()
                val cats = categoriaDao.observarPorUsuario(currentUserId).first()
                var initialCat = cats.find { it.nombre == "Saldo Inicial" }

                if (initialCat == null) {
                    val id = categoriaDao.insertar(Categoria(usuarioId = currentUserId, nombre = "Saldo Inicial")).toInt()
                    initialCat = Categoria(id = id, usuarioId = currentUserId, nombre = "Saldo Inicial")
                }

                // Limpiar registros antiguos de Saldo Inicial
                val oldBudgets = presupuestoPeriodoDao.observarPorCategoria(initialCat.id).first()
                oldBudgets.forEach { presupuestoPeriodoDao.eliminar(it) }

                presupuestoPeriodoDao.insertar(
                    PresupuestoPeriodo(
                        categoriaId = initialCat.id,
                        tipoPeriodo = TipoPeriodo.MENSUAL.name,
                        fechaInicio = 0L,
                        fechaFin = Long.MAX_VALUE,
                        montoAsignado = monto
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun configurarPresupuesto(categoriaId: Int, monto: Double, tipo: TipoPeriodo) {
        viewModelScope.launch {
            try {
                val hoy = System.currentTimeMillis()
                val fin = hoy + (30L * 24 * 60 * 60 * 1000)
                presupuestoPeriodoDao.insertar(
                    PresupuestoPeriodo(
                        categoriaId = categoriaId,
                        tipoPeriodo = tipo.name,
                        fechaInicio = hoy,
                        fechaFin = fin,
                        montoAsignado = monto
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restablecerTodo() {
        viewModelScope.launch {
            try {
                val cats = categoriaDao.observarPorUsuario(currentUserId).first()
                cats.forEach { categoriaDao.eliminar(it) }

                val loans = prestamoDao.observarPorUsuario(currentUserId).first()
                loans.forEach { prestamoDao.eliminar(it) }

                val subs = suscripcionDao.observarPorUsuario(currentUserId, "ACTIVA").first()
                subs.forEach { suscripcionDao.eliminar(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class MovimientosViewModelFactory(
    private val categoriaDao: CategoriaDao,
    private val presupuestoPeriodoDao: PresupuestoPeriodoDao,
    private val gastoDao: GastoDao,
    private val suscripcionDao: SuscripcionDao,
    private val prestamoDao: PrestamoDao,
    private val pagoPrestamoDao: PagoPrestamoDao,
    private val usuarioDao: UsuarioDao,
    private val usuarioId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovimientosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovimientosViewModel(
                categoriaDao,
                presupuestoPeriodoDao,
                gastoDao,
                suscripcionDao,
                prestamoDao,
                pagoPrestamoDao,
                usuarioDao,
                usuarioId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
