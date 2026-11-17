package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import java.time.Instant

data class OrderDto(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("category") val category: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("status") val status: OrderStatus,
    @SerializedName("message") val message: String,
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("prescription_url") val prescriptionUrl: String?,
    @SerializedName("prescription_verified") val prescriptionVerified: Boolean,
    @SerializedName("requires_prescription") val requiresPrescription: Boolean,
    @SerializedName("cancel_reason") val cancelReason: String,
    @SerializedName("created_at") val createdAt: Instant,
    @SerializedName("updated_at") val updatedAt: Instant,
    /** Only on GET orders/{id}; element shape not documented. */
    @SerializedName("sales") val sales: List<JsonObject>?, // unverified
    /** Only on cancel; element shape not documented. */
    @SerializedName("restocked") val restocked: List<JsonObject>?, // unverified
    /** Set when placed via POST checkouts (multi-item); null otherwise. */
    @SerializedName("checkout_id") val checkoutId: String? = null
)

/** POST checkouts item. Rx products need doctor_name (prescription file is uploaded afterwards). */
data class CheckoutItemRequest(
    @SerializedName("product_id") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("doctor_name") val doctorName: String? = null,
    @SerializedName("patient_name") val patientName: String? = null,
    @SerializedName("prescription_url") val prescriptionUrl: String? = null
)

/** POST checkouts (JSON). */
data class CheckoutCreateRequest(
    @SerializedName("items") val items: List<CheckoutItemRequest>,
    @SerializedName("message") val message: String? = null
)

/** data of POST checkouts: one order per item, same order as the request items. */
data class CheckoutDto(
    @SerializedName("checkout_id") val checkoutId: String,
    @SerializedName("orders") val orders: List<OrderDto>
)

/** POST orders. Sent as multipart (optional `prescription` part is passed separately). */
data class OrderCreateRequest(
    val productId: String,
    val quantity: Int,
    val message: String? = null,
    val doctorName: String? = null,
    val patientName: String? = null,
    val prescriptionUrl: String? = null
) {
    fun toParts() = textParts(
        "product_id" to productId, "quantity" to quantity.toString(), "message" to message,
        "doctor_name" to doctorName, "patient_name" to patientName, "prescription_url" to prescriptionUrl
    )
}

/** PATCH orders/{id}: own pending orders, patient_name / doctor_name only. */
data class OrderUpdateRequest(
    @SerializedName("patient_name") val patientName: String? = null,
    @SerializedName("doctor_name") val doctorName: String? = null
)

/** POST orders/{id}/cancel */
data class OrderCancelRequest(
    @SerializedName("cancel_reason") val cancelReason: String? = null
)
