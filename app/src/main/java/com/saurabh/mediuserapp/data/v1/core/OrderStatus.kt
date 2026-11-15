package com.saurabh.mediuserapp.data.v1.core

/** order `status`; unknown server values map to UNKNOWN (never crash). */
enum class OrderStatus(val wire: String) {
    PENDING("pending"),
    APPROVED("approved"),
    SOLD("sold"),
    CANCELLED("cancelled"),
    RETURNED("returned"),
    UNKNOWN("unknown");

    companion object {
        fun from(value: String?): OrderStatus {
            val v = value?.trim() ?: return UNKNOWN
            return entries.firstOrNull { it != UNKNOWN && it.wire.equals(v, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
