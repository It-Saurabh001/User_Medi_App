package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate

data class InvoiceDto(
    @SerializedName("invoice_number") val invoiceNumber: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("issued_at") val issuedAt: Instant,
    @SerializedName("generated_at") val generatedAt: Instant,
    @SerializedName("prices_include_gst") val pricesIncludeGst: Boolean,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("gst_total") val gstTotal: Double,
    @SerializedName("grand_total") val grandTotal: Double,
    @SerializedName("items") val items: List<InvoiceItemDto>,
    @SerializedName("order") val order: OrderDto,
    @SerializedName("user") val user: InvoiceUserDto
)

data class InvoiceItemDto(
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("batch_id") val batchId: String?, // unverified
    @SerializedName("batch_no") val batchNo: String?, // unverified
    @SerializedName("expiry_date") val expiryDate: LocalDate?, // unverified
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("taxable_amount") val taxableAmount: Double,
    @SerializedName("gst_percent") val gstPercent: Double,
    @SerializedName("gst_amount") val gstAmount: Double,
    @SerializedName("line_total") val lineTotal: Double
)

data class InvoiceUserDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("pin_code") val pinCode: String?
)
