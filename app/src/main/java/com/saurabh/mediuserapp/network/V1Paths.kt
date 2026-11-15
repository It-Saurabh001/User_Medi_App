package com.saurabh.mediuserapp.network

import okhttp3.Request

/** Path rules for the v1 API. v1 logic activates only when the path contains `/api/v1/`. */
internal object V1Paths {
    const val PREFIX = "/api/v1/"
    private const val REFRESH = "auth/refresh"

    private val PUBLIC_PREFIXES = listOf(
        "auth/login",
        "auth/verify-otp",
        "auth/password-reset/",
        "app-config",
        "openapi.json"
    )

    fun isV1(request: Request): Boolean = isV1Path(request.url.encodedPath)

    fun isV1Path(path: String): Boolean = path.contains(PREFIX)

    fun isHealth(path: String): Boolean = path == "/health" || path.startsWith("/health/")

    /** Path after `/api/v1/` without trailing slash, e.g. "auth/login". */
    fun relative(path: String): String = path.substringAfter(PREFIX, "").trimEnd('/')

    fun isRefresh(request: Request): Boolean = isV1(request) && relative(request.url.encodedPath) == REFRESH

    /** No Bearer: auth/login, auth/verify-otp, auth/password-reset/..., app-config, openapi.json, POST users, health. */
    fun isPublic(request: Request): Boolean {
        val path = request.url.encodedPath
        if (isHealth(path)) return true
        if (!isV1Path(path)) return false
        val rel = relative(path)
        if (rel == "users" && request.method.equals("POST", ignoreCase = true)) return true
        return PUBLIC_PREFIXES.any { rel == it.trimEnd('/') || rel.startsWith(it) }
    }
}
