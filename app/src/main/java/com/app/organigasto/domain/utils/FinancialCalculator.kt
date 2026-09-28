package com.app.organigasto.domain.utils

import com.app.organigasto.domain.model.TipoPrestamo
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

data class CalendarEvent(
    val title: String,
    val amount: Double,
    val type: String, // "INGRESO", "GASTO", "SUSCRIPCION", "PRESTAMO"
    val date: LocalDate
)

data class CategoryBudgetStatus(
    val categoriaId: Int,
    val nombre: String,
    val montoAsignado: Double,
    val montoGastado: Double,
    val restante: Double,
    val porcentajeUsado: Double,
    val excedido: Boolean,
    val colorHex: String? = null,
    val iconoNombre: String? = null,
    val iconoUri: String? = null
)

data class ResumenOrganigasto(
    val totalAsignado: Double,
    val totalGastado: Double,
    val totalDisponible: Double,
    val categoriasExcedidas: Int
)

object BudgetCalculator {
    fun calcularEstadoCategoria(
        categoriaId: Int,
        nombre: String,
        montoAsignado: Double,
        montoGastado: Double,
        colorHex: String? = null,
        iconoNombre: String? = null,
        iconoUri: String? = null
    ): CategoryBudgetStatus {
        require(montoAsignado >= 0) { "El monto asignado no puede ser negativo" }
        require(montoGastado >= 0) { "El monto gastado no puede ser negativo" }

        val restante = redondear(montoAsignado - montoGastado)
        val porcentaje = when {
            montoAsignado == 0.0 && montoGastado == 0.0 -> 0.0
            montoAsignado == 0.0 -> 100.0
            else -> (montoGastado / montoAsignado) * 100
        }

        return CategoryBudgetStatus(
            categoriaId = categoriaId,
            nombre = nombre,
            montoAsignado = montoAsignado,
            montoGastado = montoGastado,
            restante = restante,
            porcentajeUsado = redondear(porcentaje),
            excedido = montoGastado > montoAsignado,
            colorHex = colorHex,
            iconoNombre = iconoNombre,
            iconoUri = iconoUri
        )
    }

    fun calcularResumenGeneral(categorias: List<CategoryBudgetStatus>): ResumenOrganigasto {
        val totalAsignado = redondear(categorias.sumOf { it.montoAsignado })
        val totalGastado = redondear(categorias.sumOf { it.montoGastado })
        return ResumenOrganigasto(
            totalAsignado = totalAsignado,
            totalGastado = totalGastado,
            totalDisponible = redondear(totalAsignado - totalGastado),
            categoriasExcedidas = categorias.count { it.excedido }
        )
    }

    /**
     * Calcula cuánto dinero puede gastar el usuario por día para no excederse.
     */
    fun calcularGastoDiarioPermitido(
        status: CategoryBudgetStatus,
        diasRestantes: Int
    ): Double {
        if (diasRestantes <= 0) return 0.0
        return redondear(status.restante.coerceAtLeast(0.0) / diasRestantes)
    }
}

data class GastoPorCategoria(
    val nombreCategoria: String,
    val monto: Double
)

data class DistribucionGasto(
    val nombreCategoria: String,
    val monto: Double,
    val porcentaje: Double
)

data class ComparativoMensual(
    val totalMesActual: Double,
    val totalMesAnterior: Double,
    val variacionPorcentual: Double,
    val gastoMasQueElMesPasado: Boolean
)

object StatisticsCalculator {
    fun calcularDistribucionPorCategoria(gastos: List<GastoPorCategoria>): List<DistribucionGasto> {
        val total = gastos.sumOf { it.monto }
        if (total == 0.0) {
            return gastos.map { DistribucionGasto(it.nombreCategoria, 0.0, 0.0) }
        }
        return gastos.map {
            DistribucionGasto(
                nombreCategoria = it.nombreCategoria,
                monto = it.monto,
                porcentaje = redondear((it.monto / total) * 100)
            )
        }
    }

    fun compararConMesAnterior(totalMesActual: Double, totalMesAnterior: Double): ComparativoMensual {
        val variacion = when {
            totalMesAnterior == 0.0 && totalMesActual == 0.0 -> 0.0
            totalMesAnterior == 0.0 -> 100.0
            else -> ((totalMesActual - totalMesAnterior) / totalMesAnterior) * 100
        }
        return ComparativoMensual(
            totalMesActual = totalMesActual,
            totalMesAnterior = totalMesAnterior,
            variacionPorcentual = redondear(variacion),
            gastoMasQueElMesPasado = totalMesActual > totalMesAnterior
        )
    }
}

data class FilaAmortizacion(
    val numeroPago: Int,
    val pagoMensual: Double,
    val interesPagado: Double,
    val capitalPagado: Double,
    val saldoRestante: Double
)

data class ResumenPrestamo(
    val montoOriginal: Double,
    val meses: Int,
    val tipo: TipoPrestamo,
    val tasaAnual: Double,
    val pagoMensual: Double,
    val totalPagado: Double,
    val totalIntereses: Double,
    val porcentajeInteresSobreMonto: Double,
    val tablaAmortizacion: List<FilaAmortizacion>
)

object LoanCalculator {
    fun calcularPrestamo(
        monto: Double,
        meses: Int,
        tipo: TipoPrestamo,
        tasaAnual: Double = 0.0
    ): ResumenPrestamo {
        require(monto > 0) { "El monto debe ser mayor a 0" }
        require(meses > 0) { "El plazo debe ser mayor a 0 meses" }

        return when (tipo) {
            TipoPrestamo.SIN_INTERESES -> calcularMSI(monto, meses)
            TipoPrestamo.CON_INTERES -> {
                require(tasaAnual > 0) { "La tasa anual debe ser mayor a 0 para un préstamo con interés" }
                calcularConInteres(monto, meses, tasaAnual)
            }
        }
    }

    private fun calcularMSI(monto: Double, meses: Int): ResumenPrestamo {
        val pagoBase = redondear(monto / meses)
        val tabla = mutableListOf<FilaAmortizacion>()
        var saldo = monto

        for (i in 1..meses) {
            val pago = if (i == meses) redondear(saldo) else pagoBase
            saldo = redondear(saldo - pago)
            tabla.add(FilaAmortizacion(i, pago, interesPagado = 0.0, capitalPagado = pago, saldoRestante = saldo))
        }

        return ResumenPrestamo(
            montoOriginal = monto,
            meses = meses,
            tipo = TipoPrestamo.SIN_INTERESES,
            tasaAnual = 0.0,
            pagoMensual = pagoBase,
            totalPagado = monto,
            totalIntereses = 0.0,
            porcentajeInteresSobreMonto = 0.0,
            tablaAmortizacion = tabla
        )
    }

    private fun calcularConInteres(monto: Double, meses: Int, tasaAnual: Double): ResumenPrestamo {
        val tasaMensual = (tasaAnual / 100) / 12
        val factor = (1 + tasaMensual).pow(meses)
        val pagoMensual = redondear(monto * (tasaMensual * factor) / (factor - 1))

        val tabla = mutableListOf<FilaAmortizacion>()
        var saldo = monto
        var totalPagadoAcumulado = 0.0

        for (i in 1..meses) {
            val interesDelMes = redondear(saldo * tasaMensual)
            var pago = pagoMensual
            var capitalDelMes = redondear(pago - interesDelMes)

            if (i == meses) {
                capitalDelMes = saldo
                pago = redondear(capitalDelMes + interesDelMes)
            }

            saldo = redondear(saldo - capitalDelMes)
            totalPagadoAcumulado = redondear(totalPagadoAcumulado + pago)

            tabla.add(FilaAmortizacion(i, pago, interesDelMes, capitalDelMes, saldo))
        }

        val totalIntereses = redondear(totalPagadoAcumulado - monto)
        val porcentajeInteres = redondear((totalIntereses / monto) * 100)

        return ResumenPrestamo(
            montoOriginal = monto,
            meses = meses,
            tipo = TipoPrestamo.CON_INTERES,
            tasaAnual = tasaAnual,
            pagoMensual = pagoMensual,
            totalPagado = totalPagadoAcumulado,
            totalIntereses = totalIntereses,
            porcentajeInteresSobreMonto = porcentajeInteres,
            tablaAmortizacion = tabla
        )
    }

    fun compararMSIvsInteres(monto: Double, meses: Int, tasaAnual: Double): Pair<ResumenPrestamo, ResumenPrestamo> {
        val msi = calcularPrestamo(monto, meses, TipoPrestamo.SIN_INTERESES)
        val conInteres = calcularPrestamo(monto, meses, TipoPrestamo.CON_INTERES, tasaAnual)
        return msi to conInteres
    }

    /**
     * Ordena las deudas según la estrategia elegida.
     * @param avalancha true para mayor interés primero, false para menor monto primero (bola nieve).
     */
    fun sugerirOrdenPago(prestamos: List<ResumenPrestamo>, avalancha: Boolean): List<ResumenPrestamo> {
        return if (avalancha) {
            prestamos.sortedByDescending { it.tasaAnual }
        } else {
            prestamos.sortedBy { it.montoOriginal }
        }
    }

    /**
     * Simula el impacto de un pago extra a capital en un préstamo.
     */
    fun simularImpactoPagoExtra(
        prestamo: ResumenPrestamo,
        montoExtra: Double
    ): ImpactoSimulado {
        if (montoExtra <= 0) return ImpactoSimulado(0, 0.0)
        
        // Versión simplificada: Calculamos cuántos pagos mensuales "compra" el pago extra
        // En una amortización real, esto reduce el capital y recalcula intereses futuros.
        val mesesAhorrados = (montoExtra / prestamo.pagoMensual).toInt()
        val interesAhorrado = redondear(mesesAhorrados * (prestamo.totalIntereses / prestamo.meses))
        
        return ImpactoSimulado(
            mesesAhorrados = mesesAhorrados,
            interesAhorrado = interesAhorrado
        )
    }
}

data class ImpactoSimulado(
    val mesesAhorrados: Int,
    val interesAhorrado: Double
)

// ---------------------------------------------------------------
// 4. MÓDULO FLUJO DE CAJA (PROYECCIÓN)
// ---------------------------------------------------------------

data class ProyeccionSaldo(
    val saldoActual: Double, // Dinero real hoy
    val ingresosPendientes: Double,
    val gastosProyectados: Double,
    val cuotasPrestamosPendientes: Double,
    val suscripcionesPendientes: Double,
    val saldoFinalProyectado: Double
)

object CashFlowCalculator {
    /**
     * Calcula cuánto dinero quedará al final del periodo considerando
     * todos los compromisos financieros.
     */
    fun calcularProyeccion(
        saldoInicial: Double,
        ingresosRealizados: Double,
        gastosRealizados: Double,
        cuotasPagadas: Double,
        suscripcionesPagadas: Double,
        ingresosPendientes: List<Double>,
        gastosPresupuestados: List<Double>,
        cuotasPrestamosPendientes: List<Double>,
        suscripcionesPendientes: List<Double>
    ): ProyeccionSaldo {
        val saldoHoy = saldoInicial + ingresosRealizados - gastosRealizados - cuotasPagadas - suscripcionesPagadas

        val totalIngresosPend = ingresosPendientes.sum()
        val totalGastosPend = gastosPresupuestados.sum()
        val totalCuotasPend = cuotasPrestamosPendientes.sum()
        val totalSuscripcionesPend = suscripcionesPendientes.sum()

        val saldoFinal = saldoHoy + totalIngresosPend - totalGastosPend - totalCuotasPend - totalSuscripcionesPend

        return ProyeccionSaldo(
            saldoActual = redondear(saldoHoy),
            ingresosPendientes = redondear(totalIngresosPend),
            gastosProyectados = redondear(totalGastosPend),
            cuotasPrestamosPendientes = redondear(totalCuotasPend),
            suscripcionesPendientes = redondear(totalSuscripcionesPend),
            saldoFinalProyectado = redondear(saldoFinal)
        )
    }
}

// ---------------------------------------------------------------
// 5. MÓDULO FORECASTING (PREDICCIONES)
// ---------------------------------------------------------------

data class CategoryForecast(
    val categoriaId: Int,
    val nombre: String,
    val diasRestantesEstimados: Int, // Cuántos días antes de que se agote el presupuesto
    val agotamientoAntesDeSueldo: Boolean,
    val sugerencia: String?
)

object ForecastingCalculator {
    /**
     * Predice cuándo se agotará un presupuesto basándose en el ritmo de gasto.
     */
    fun predecirAgotamiento(
        status: CategoryBudgetStatus,
        diasTranscurridos: Int,
        diasHastaSueldo: Int
    ): CategoryForecast {
        val promedioDiario = if (diasTranscurridos > 0) status.montoGastado / diasTranscurridos else 0.0
        
        val diasRestantes = if (promedioDiario > 0) {
            (status.restante / promedioDiario).toInt()
        } else {
            30 // Asumimos un mes si no hay gasto
        }

        val enRiesgo = diasRestantes < diasHastaSueldo && status.restante > 0
        
        val sugerencia = if (enRiesgo) {
            val balanceDiario = (status.restante / diasHastaSueldo)
            "Reduce tu gasto diario a $${String.format(java.util.Locale.US, "%.2f", balanceDiario)} para llegar al día de pago."
        } else null

        return CategoryForecast(
            categoriaId = status.categoriaId,
            nombre = status.nombre,
            diasRestantesEstimados = diasRestantes.coerceIn(0, 99),
            agotamientoAntesDeSueldo = enRiesgo,
            sugerencia = sugerencia
        )
    }
}

// ---------------------------------------------------------------
// 6. MÓDULO PROVISIONES (SINKING FUNDS)
// ---------------------------------------------------------------

data class ProvisionMeta(
    val nombre: String,
    val montoObjetivo: Double,
    val fechaMeta: java.time.LocalDate,
    val montoAhorrado: Double
)

data class EstadoProvision(
    val nombre: String,
    val cuotaMensualNecesaria: Double,
    val mesesRestantes: Int,
    val porcentajeCompletado: Double
)

object ProvisionCalculator {
    fun calcularEstado(meta: ProvisionMeta): EstadoProvision {
        val hoy = LocalDate.now()
        val meses = ChronoUnit.MONTHS.between(hoy, meta.fechaMeta).toInt().coerceAtLeast(1)
        val faltante = (meta.montoObjetivo - meta.montoAhorrado).coerceAtLeast(0.0)
        
        return EstadoProvision(
            nombre = meta.nombre,
            cuotaMensualNecesaria = redondear(faltante / meses),
            mesesRestantes = meses,
            porcentajeCompletado = redondear((meta.montoAhorrado / meta.montoObjetivo) * 100)
        )
    }
}

// ---------------------------------------------------------------
// 7. MÓDULO VOLATILIDAD (STRESS TEST)
// ---------------------------------------------------------------

data class ReporteVolatilidad(
    val diaMasCritico: Int, // Día del mes con menor saldo
    val saldoMinimoEstimado: Double,
    val indiceEstabilidad: Int // 0-100
)

object VolatilityCalculator {
    /**
     * Identifica el punto de quiebre en el flujo de caja.
     */
    fun analizarEstabilidad(
        saldoInicial: Double,
        eventos: List<CalendarEvent>
    ): ReporteVolatilidad {
        var saldoMinimo = saldoInicial
        var diaMinimo = java.time.LocalDate.now().dayOfMonth
        var saldoCorriente = saldoInicial
        
        // Ordenamos eventos por día del mes
        val eventosOrdenados = eventos.sortedBy { it.date.dayOfMonth }
        
        for (evento in eventosOrdenados) {
            if (evento.type == "INGRESO") saldoCorriente += evento.amount
            else saldoCorriente -= evento.amount
            
            if (saldoCorriente < saldoMinimo) {
                saldoMinimo = saldoCorriente
                diaMinimo = evento.date.dayOfMonth
            }
        }
        
        val estabilidad = if (saldoMinimo < 0) 0 else ((saldoMinimo / (saldoInicial + 1)) * 100).toInt().coerceIn(0, 100)
        
        return ReporteVolatilidad(
            diaMasCritico = diaMinimo,
            saldoMinimoEstimado = redondear(saldoMinimo),
            indiceEstabilidad = estabilidad
        )
    }
}

private fun redondear(valor: Double): Double =
    BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toDouble()
