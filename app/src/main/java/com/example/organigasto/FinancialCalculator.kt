package com.example.organigasto

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

/**
 * ==============================================================
 *  LÓGICA DE CÁLCULO FINANCIERO - Organigasto
 * ==============================================================
 * Este archivo NO depende de Room ni de Android: son funciones puras
 * (misma entrada -> misma salida), así que las puedes probar con
 * JUnit sin necesitar un emulador ni una base de datos real.
 *
 * Se divide en 3 partes, una por módulo:
 *   1. BudgetCalculator      -> Módulo "Organigasto" (sobres/categorías)
 *   2. StatisticsCalculator  -> Módulo "Estadísticas"
 *   3. LoanCalculator        -> Módulo "Préstamos" (MSI y con interés)
 *
 * Tus Entities/DAOs de Room le entregan los números (Double) a estas
 * funciones, y estas funciones regresan objetos listos para pintar
 * en pantalla (en un ViewModel, por ejemplo).
 * ==============================================================
 */

// ---------------------------------------------------------------
// 1. MÓDULO ORGANIGASTO
// ---------------------------------------------------------------

/** Estado de una categoría ("sobre") del organigasto. */
data class CategoryBudgetStatus(
    val categoriaId: Long,
    val nombre: String,
    val montoAsignado: Double,
    val montoGastado: Double,
    val restante: Double,
    val porcentajeUsado: Double, // 0-100 (puede pasar de 100 si se excede)
    val excedido: Boolean
)

data class ResumenOrganigasto(
    val totalAsignado: Double,
    val totalGastado: Double,
    val totalDisponible: Double,
    val categoriasExcedidas: Int
)

object BudgetCalculator {

    /**
     * Calcula el estado de una categoría del organigasto.
     * @param montoAsignado cuánto se apartó para esa categoría este mes
     * @param montoGastado suma de los gastos ya registrados en esa categoría
     */
    fun calcularEstadoCategoria(
        categoriaId: Long,
        nombre: String,
        montoAsignado: Double,
        montoGastado: Double
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
            excedido = montoGastado > montoAsignado
        )
    }

    /** Resumen general del organigasto completo (todas las categorías juntas). */
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
}

// ---------------------------------------------------------------
// 2. MÓDULO ESTADÍSTICAS
// ---------------------------------------------------------------

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
    val variacionPorcentual: Double, // positivo = gastaste más, negativo = menos
    val gastoMasQueElMesPasado: Boolean
)

object StatisticsCalculator {

    /**
     * Convierte una lista de gastos por categoría en su distribución
     * porcentual, lista para alimentar una gráfica de pastel/dona.
     */
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

    /** Compara el gasto total de este mes contra el mes anterior. */
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

// ---------------------------------------------------------------
// 3. MÓDULO PRÉSTAMOS
// ---------------------------------------------------------------

enum class TipoPrestamo {
    SIN_INTERESES,  // MSI
    CON_INTERES
}

/** Una fila de la tabla de amortización: un pago mensual. */
data class FilaAmortizacion(
    val numeroPago: Int,
    val pagoMensual: Double,
    val interesPagado: Double,
    val capitalPagado: Double,
    val saldoRestante: Double
)

/** Resumen completo de un préstamo, listo para mostrar en pantalla. */
data class ResumenPrestamo(
    val montoOriginal: Double,
    val meses: Int,
    val tipo: TipoPrestamo,
    val tasaAnual: Double,                     // 0.0 si es MSI
    val pagoMensual: Double,
    val totalPagado: Double,
    val totalIntereses: Double,
    val porcentajeInteresSobreMonto: Double,    // ej. 18.5 => pagas 18.5% extra sobre el monto original
    val tablaAmortizacion: List<FilaAmortizacion>
)

object LoanCalculator {

    /**
     * Punto de entrada único: calcula todo lo que necesita el módulo de
     * préstamos, ya sea MSI o con interés.
     *
     * @param monto monto total del préstamo
     * @param meses plazo en meses
     * @param tasaAnual tasa de interés ANUAL en porcentaje (ej. 36.0 para 36%).
     *                   Se ignora si tipo == SIN_INTERESES.
     */
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

    /**
     * Meses Sin Intereses: el pago mensual es el monto dividido entre el
     * plazo. No hay interés que calcular, solo se reparte el capital.
     */
    private fun calcularMSI(monto: Double, meses: Int): ResumenPrestamo {
        val pagoBase = redondear(monto / meses)
        val tabla = mutableListOf<FilaAmortizacion>()
        var saldo = monto

        for (i in 1..meses) {
            // En el último pago se ajusta el centavo restante para que el saldo cierre en 0
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

    /**
     * Préstamo con interés, sistema francés (pago mensual FIJO).
     * Es el sistema que usan casi todos los créditos personales y de tarjeta.
     *
     * Fórmula del pago fijo:
     *   A = P * ( i * (1+i)^n ) / ( (1+i)^n - 1 )
     * donde:
     *   P = monto del préstamo
     *   i = tasa de interés MENSUAL en decimal (tasa anual / 100 / 12)
     *   n = número de meses
     */
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

            // Último pago: se ajusta para saldar exactamente lo que quede (evita centavos sueltos)
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

    /**
     * Compara un mismo monto/plazo bajo MSI vs. con interés: ideal para
     * mostrar el "vs" en pantalla que pediste (Módulo Préstamos).
     */
    fun compararMSIvsInteres(monto: Double, meses: Int, tasaAnual: Double): Pair<ResumenPrestamo, ResumenPrestamo> {
        val msi = calcularPrestamo(monto, meses, TipoPrestamo.SIN_INTERESES)
        val conInteres = calcularPrestamo(monto, meses, TipoPrestamo.CON_INTERES, tasaAnual)
        return msi to conInteres
    }
}

// ---------------------------------------------------------------
// Utilidad compartida
// ---------------------------------------------------------------

/** Redondea a 2 decimales evitando errores de punto flotante en dinero. */
private fun redondear(valor: Double): Double =
    BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toDouble()
