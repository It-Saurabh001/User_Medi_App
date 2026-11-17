package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.CheckoutDto
import com.saurabh.mediuserapp.data.v1.error.AppError

/** One cart line (in-memory). [rxUri] is the picked prescription file (content:// string), uploaded after checkout. */
data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String?,
    val dosageForm: String?,
    val quantity: Int,
    val maxQuantity: Int,
    val needsPrescription: Boolean,
    val doctorName: String = "",
    val patientName: String = "",
    val rxUri: String? = null,
)

/**
 * POST checkouts result. [itemErrors] = cart index -> message (from `fields["items[i].field"]`).
 * [idempotencyKey] is kept across retries of the same unchanged cart.
 */
data class CheckoutState(
    val isLoading: Boolean = false,
    val success: CheckoutDto? = null,
    val error: String? = null,
    val appError: AppError? = null,
    val itemErrors: Map<Int, String> = emptyMap(),
    val idempotencyKey: String? = null,
    val uploadFailures: List<String> = emptyList(),
)
