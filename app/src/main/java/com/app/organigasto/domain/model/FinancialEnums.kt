package com.app.organigasto.domain.model

enum class TipoPeriodo { SEMANAL, QUINCENAL, MENSUAL }
enum class FrecuenciaSuscripcion { MENSUAL, ANUAL }
enum class EstadoSuscripcion { ACTIVA, PAUSADA, CANCELADA }
enum class EstadoCobro { PENDIENTE, COBRADO, FALLIDO }

enum class TipoPrestamo {
    SIN_INTERESES,  // MSI
    CON_INTERES
}
