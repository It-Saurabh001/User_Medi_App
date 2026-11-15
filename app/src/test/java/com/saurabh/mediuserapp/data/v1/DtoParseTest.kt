package com.saurabh.mediuserapp.data.v1

import com.google.gson.reflect.TypeToken
import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.core.PagedEnvelope
import com.saurabh.mediuserapp.data.v1.core.V1Gson
import com.saurabh.mediuserapp.data.v1.dto.AppConfig
import com.saurabh.mediuserapp.data.v1.dto.InvoiceDto
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.data.v1.dto.UserDto
import com.saurabh.mediuserapp.data.v1.error.ErrorBody
import com.saurabh.mediuserapp.data.v1.error.ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DtoParseTest {

    private val gson = V1Gson.instance

    private inline fun <reified T> envelope(json: String): ApiEnvelope<T> =
        gson.fromJson(json, TypeToken.getParameterized(ApiEnvelope::class.java, T::class.java).type)

    private inline fun <reified T> paged(json: String): PagedEnvelope<T> =
        gson.fromJson(json, TypeToken.getParameterized(PagedEnvelope::class.java, T::class.java).type)

    private val orderJson = """{
      "cancel_reason": "", "category": "Analgesic", "created_at": "2026-11-14T17:33:50Z",
      "doctor_name": "", "image_url": null, "message": "Need by evening", "order_id": "ORD_f828d7b2e6d6",
      "patient_name": "Priya Sharma", "prescription_url": null, "prescription_verified": false,
      "price": 31.0, "product_id": "PROD_c7a36281e0", "product_name": "Paracetamol 500mg", "quantity": 2,
      "requires_prescription": false, "status": "sold", "total_amount": 62.0,
      "updated_at": "2026-11-14T17:33:51Z", "user_id": "UID_06ade2a9b763", "user_name": "Priya S"
    }"""

    @Test
    fun user() {
        val e = envelope<UserDto>("""{"status":201,"message":"Account created","data":{
          "address":"12 MG Road, Pune","created_at":"2026-11-14T17:33:47Z","email":"priya@example.com",
          "image_url":null,"is_approved":false,"is_blocked":false,"is_deleted":false,"name":"Priya Sharma",
          "phone_number":"+919811111111","pin_code":"411001","role":"user","user_id":"UID_06ade2a9b763"}}""")
        val u = e.data!!
        assertEquals("UID_06ade2a9b763", u.userId)
        assertFalse(u.isApproved)
        assertNull(u.imageUrl)
        assertEquals(Instant.parse("2026-11-14T17:33:47Z"), u.createdAt)
    }

    @Test
    fun product_pagedList() {
        val p = paged<ProductDto>("""{"status":200,"message":"ok","data":[{
          "barcode":"","brand_name":"","category":"General","created_at":"2026-11-14T17:33:50Z",
          "dosage_form":"Tablet","generic_name":"","gst_percent":12.0,"hsn_code":"","image_url":null,
          "in_stock":false,"is_active":true,"manufacturer":"Unknown","mrp":125.0,"name":"Azithromycin 500mg",
          "pack_size":"","price":120.0,"product_id":"PROD_20f28a6512","purchase_price":null,"reorder_level":10,
          "requires_prescription":true,"schedule_type":"H","sellable_stock":0,"stock":0,"storage_conditions":"",
          "strength":"","unit":"strip","updated_at":"2026-11-14T17:33:50Z"}],
          "pagination":{"limit":2,"offset":0,"total":2}}""")
        val item = p.data!!.single()
        assertEquals("PROD_20f28a6512", item.productId)
        assertEquals(120.0, item.price, 0.0)
        assertNull(item.purchasePrice)
        assertTrue(item.requiresPrescription)
        assertNotNull(item.createdAt)
        assertEquals(2, p.pagination?.total)
    }

    @Test
    fun order() {
        val o = envelope<OrderDto>("""{"status":200,"message":"ok","data":$orderJson}""").data!!
        assertEquals(OrderStatus.SOLD, o.status)
        assertEquals(62.0, o.totalAmount, 0.0)
        assertNull(o.prescriptionUrl)
        assertNull(o.sales)
        assertNotNull(o.updatedAt)
    }

    @Test
    fun order_withSalesAndRestocked() {
        val o = envelope<OrderDto>(
            """{"status":200,"message":"ok","data":${orderJson.trimEnd().dropLast(1)}, "sales": [], "restocked": []}}"""
        ).data!!
        assertTrue(o.sales!!.isEmpty())
        assertTrue(o.restocked!!.isEmpty())
    }

    @Test
    fun invoice() {
        val json = """{"status":200,"message":"Invoice generated","data":{
          "currency":"INR","generated_at":"2026-11-14T17:33:51Z","grand_total":62.0,"gst_total":6.64,
          "invoice_number":"INV-ORD_f828d7b2e6d6","issued_at":"2026-11-14T17:33:51Z",
          "items":[{"batch_id":"BAT_2cceba2ef4f8","batch_no":"B-2026-01","expiry_date":"2028-06-30",
            "gst_amount":6.64,"gst_percent":12.0,"line_total":62.0,"product_id":"PROD_c7a36281e0",
            "product_name":"Paracetamol 500mg","quantity":2,"taxable_amount":55.36,"unit_price":31.0}],
          "order":$orderJson,"prices_include_gst":true,"subtotal":55.36,
          "user":{"address":"14 MG Road, Pune","email":"priya@example.com","name":"Priya S",
            "phone_number":"+919811111111","pin_code":"411002","user_id":"UID_06ade2a9b763"}}}"""
        val inv = envelope<InvoiceDto>(json).data!!
        assertEquals("INV-ORD_f828d7b2e6d6", inv.invoiceNumber)
        assertEquals(LocalDate.of(2028, 6, 30), inv.items.single().expiryDate)
        assertEquals(OrderStatus.SOLD, inv.order.status)
        assertEquals("UID_06ade2a9b763", inv.user.userId)
        assertTrue(inv.pricesIncludeGst)
    }

    @Test
    fun appConfig() {
        val json = """{"status":200,"message":"App configuration","data":{
          "api_prefix":"/api/v1","api_version":"v1","docs_enabled":true,
          "enums":{"account_types":["user","admin"],"device_platforms":["android","ios"],
            "dosage_forms":["Tablet"],"order_statuses":["pending","approved","sold","cancelled","returned"],
            "prescription_schedules":["H"],"schedule_types":["OTC","H"],
            "stock_movement_reasons":["PURCHASE"],"units":["strip"]},
          "error_codes":["VALIDATION_ERROR","NOT_FOUND"],"min_app_version":null,
          "otp":{"expires_in":300,"length":6,"max_attempts":5},
          "pagination":{"default_limit":20,"max_limit":100},"server_time":"2026-11-14T17:33:45Z",
          "uploads":{"allowed_mime_types":["image/jpeg","application/pdf"],"image_mime_types":["image/jpeg"],
            "max_request_bytes":6291456,"max_upload_mb":5}}}"""
        val c = envelope<AppConfig>(json).data!!
        assertNull(c.minAppVersion)
        assertEquals(6291456L, c.uploads.maxRequestBytes)
        assertEquals(100, c.pagination.maxLimit)
        assertEquals(5, c.otp.maxAttempts)
        assertEquals(Instant.parse("2026-11-14T17:33:45Z"), c.serverTime)
    }

    @Test
    fun errorWithFields() {
        val e = ErrorBody.parse("""{"code":"VALIDATION_ERROR","fields":{"email":"must be a valid email address",
          "password":"must be at least 8 characters"},"message":"Validation failed","status":400}""")!!
        assertEquals(ErrorCode.VALIDATION_ERROR, ErrorCode.from(e.code))
        assertEquals(2, e.fields!!.size)
        assertEquals("Validation failed", e.message)
    }
}
