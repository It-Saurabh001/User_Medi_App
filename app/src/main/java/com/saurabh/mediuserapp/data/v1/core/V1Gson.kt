package com.saurabh.mediuserapp.data.v1.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/** Gson for the v1 Retrofit ONLY (old DTOs keep the default Gson). Money = Double. */
object V1Gson {
    val instance: Gson by lazy { create() }

    fun create(): Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter)
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .registerTypeAdapter(OrderStatus::class.java, OrderStatusAdapter)
        .registerTypeAdapter(ErrorCode::class.java, ErrorCodeAdapter)
        .create()

    /** ISO `...Z`, offset, local date-time (as UTC) or date-only `YYYY-MM-DD` (00:00 UTC). Blank/invalid -> null. */
    fun parseInstant(raw: String?): Instant? {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty()) return null
        return runCatching { Instant.parse(s) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(s).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(s).toInstant(ZoneOffset.UTC) }.getOrNull()
            ?: runCatching { LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant() }.getOrNull()
    }

    /** `YYYY-MM-DD` (or a timestamp, taken as its UTC date). Blank/invalid -> null. */
    fun parseLocalDate(raw: String?): LocalDate? {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty()) return null
        return runCatching { LocalDate.parse(s) }.getOrNull()
            ?: parseInstant(s)?.atZone(ZoneOffset.UTC)?.toLocalDate()
    }
}

private fun JsonReader.nextStringOrNull(): String? =
    if (peek() == JsonToken.NULL) {
        nextNull(); null
    } else {
        nextString()
    }

object InstantAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) out.nullValue() else out.value(value.toString())
    }
    override fun read(reader: JsonReader): Instant? = V1Gson.parseInstant(reader.nextStringOrNull())
}

object LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) out.nullValue() else out.value(value.toString())
    }
    override fun read(reader: JsonReader): LocalDate? = V1Gson.parseLocalDate(reader.nextStringOrNull())
}

object OrderStatusAdapter : TypeAdapter<OrderStatus>() {
    override fun write(out: JsonWriter, value: OrderStatus?) {
        if (value == null) out.nullValue() else out.value(value.wire)
    }
    override fun read(reader: JsonReader): OrderStatus? = reader.nextStringOrNull()?.let { OrderStatus.from(it) }
}

object ErrorCodeAdapter : TypeAdapter<ErrorCode>() {
    override fun write(out: JsonWriter, value: ErrorCode?) {
        if (value == null) out.nullValue() else out.value(value.name)
    }
    override fun read(reader: JsonReader): ErrorCode? = reader.nextStringOrNull()?.let { ErrorCode.from(it) }
}
