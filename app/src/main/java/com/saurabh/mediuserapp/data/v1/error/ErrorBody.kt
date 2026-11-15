package com.saurabh.mediuserapp.data.v1.error

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

/** v1 error body: {"status","message","code","fields"?}. */
data class ErrorBody(
    @SerializedName("status") val status: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("fields") val fields: Map<String, String>?
) {
    companion object {
        /** Tolerant: null for blank / non-JSON input; non-string field values are stringified. */
        fun parse(raw: String?): ErrorBody? {
            if (raw.isNullOrBlank()) return null
            val obj: JsonObject = try {
                JsonParser.parseString(raw).takeIf { it.isJsonObject }?.asJsonObject
            } catch (_: Exception) {
                null
            } ?: return null

            fun str(key: String): String? = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString

            val fields = obj.get("fields")?.takeIf { it.isJsonObject }?.asJsonObject?.entrySet()
                ?.associate { (k, v) -> k to if (v.isJsonPrimitive) v.asString else v.toString() }
            val status = obj.get("status")
                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt
            return ErrorBody(status = status, message = str("message"), code = str("code"), fields = fields)
        }
    }
}
