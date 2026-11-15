package com.saurabh.mediuserapp.data.v1.core

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Value for the optional `Idempotency-Key` header (POST orders). Reuse the same key when retrying. */
@Singleton
class IdempotencyKeyProvider @Inject constructor() {
    fun newKey(): String = UUID.randomUUID().toString()

    companion object {
        const val HEADER = "Idempotency-Key"
    }
}
