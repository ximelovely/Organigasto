package com.example.organigasto

import androidx.room.TypeConverter

/**
 * Room no sabe guardar enums directamente: hay que decirle cómo
 * convertirlos a String (para guardar) y de regreso a enum (para leer).
 */
class Converters {

    @TypeConverter
    fun fromTipoPeriodo(valor: TipoPeriodo): String = valor.name
    @TypeConverter
    fun toTipoPeriodo(valor: String): TipoPeriodo = TipoPeriodo.valueOf(valor)

    @TypeConverter
    fun fromFrecuenciaSuscripcion(valor: FrecuenciaSuscripcion): String = valor.name
    @TypeConverter
    fun toFrecuenciaSuscripcion(valor: String): FrecuenciaSuscripcion = FrecuenciaSuscripcion.valueOf(valor)

    @TypeConverter
    fun fromEstadoSuscripcion(valor: EstadoSuscripcion): String = valor.name
    @TypeConverter
    fun toEstadoSuscripcion(valor: String): EstadoSuscripcion = EstadoSuscripcion.valueOf(valor)

    @TypeConverter
    fun fromEstadoCobro(valor: EstadoCobro): String = valor.name
    @TypeConverter
    fun toEstadoCobro(valor: String): EstadoCobro = EstadoCobro.valueOf(valor)

    @TypeConverter
    fun fromTipoPrestamo(valor: TipoPrestamo): String = valor.name
    @TypeConverter
    fun toTipoPrestamo(valor: String): TipoPrestamo = TipoPrestamo.valueOf(valor)
}
