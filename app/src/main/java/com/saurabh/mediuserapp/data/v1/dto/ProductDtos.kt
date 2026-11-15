package com.saurabh.mediuserapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class ProductDto(
    @SerializedName("product_id") val productId: String,
    @SerializedName("name") val name: String,
    @SerializedName("generic_name") val genericName: String,
    @SerializedName("brand_name") val brandName: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("category") val category: String,
    @SerializedName("dosage_form") val dosageForm: String,
    @SerializedName("strength") val strength: String,
    @SerializedName("pack_size") val packSize: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("hsn_code") val hsnCode: String,
    @SerializedName("gst_percent") val gstPercent: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("mrp") val mrp: Double,
    @SerializedName("purchase_price") val purchasePrice: Double?, // always null for customers
    @SerializedName("schedule_type") val scheduleType: String,
    @SerializedName("requires_prescription") val requiresPrescription: Boolean,
    @SerializedName("storage_conditions") val storageConditions: String,
    @SerializedName("reorder_level") val reorderLevel: Int,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("stock") val stock: Int,
    @SerializedName("sellable_stock") val sellableStock: Int,
    @SerializedName("in_stock") val inStock: Boolean,
    @SerializedName("created_at") val createdAt: Instant,
    @SerializedName("updated_at") val updatedAt: Instant
)

data class ProductCategoryDto(
    @SerializedName("name") val name: String,
    @SerializedName("product_count") val productCount: Int
)
