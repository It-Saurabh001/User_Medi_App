package com.saurabh.mediuserapp.data.v1.api

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.PagedEnvelope
import com.saurabh.mediuserapp.data.v1.dto.CheckoutCreateRequest
import com.saurabh.mediuserapp.data.v1.dto.CheckoutDto
import com.saurabh.mediuserapp.data.v1.dto.InvoiceDto
import com.saurabh.mediuserapp.data.v1.dto.OrderCancelRequest
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.data.v1.dto.OrderUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/** Customer orders. Users only ever see their own orders (others -> 404). */
interface OrdersApi {
    /** Fields via OrderCreateRequest.toParts(); [prescription] optional; [idempotencyKey] from IdempotencyKeyProvider. */
    @Multipart
    @POST("orders")
    suspend fun createOrder(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part prescription: MultipartBody.Part? = null,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<ApiEnvelope<OrderDto>>

    /** status: pending|approved|sold|cancelled|returned; from/to: YYYY-MM-DD; sort: created_at|updated_at|total_amount|status. */
    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("product_id") productId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("updated_since") updatedSince: String? = null,
        @Query("checkout_id") checkoutId: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<OrderDto>>

    @GET("orders/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: String): Response<ApiEnvelope<OrderDto>>

    @PATCH("orders/{order_id}")
    suspend fun updateOrder(
        @Path("order_id") orderId: String,
        @Body body: OrderUpdateRequest
    ): Response<ApiEnvelope<OrderDto>>

    @POST("orders/{order_id}/cancel")
    suspend fun cancelOrder(
        @Path("order_id") orderId: String,
        @Body body: OrderCancelRequest = OrderCancelRequest()
    ): Response<ApiEnvelope<OrderDto>>

    @GET("orders/{order_id}/invoice")
    suspend fun getInvoice(@Path("order_id") orderId: String): Response<ApiEnvelope<InvoiceDto>>

    /** Several products in one call (JSON); one order per item grouped by checkout_id. */
    @POST("checkouts")
    suspend fun createCheckout(
        @Body body: CheckoutCreateRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<ApiEnvelope<CheckoutDto>>

    /** Combined invoice of the sold/returned orders of a checkout. */
    @GET("checkouts/{checkout_id}/invoice")
    suspend fun getCheckoutInvoice(@Path("checkout_id") checkoutId: String): Response<ApiEnvelope<InvoiceDto>>

    /** [fields]: optional doctor_name / patient_name text parts. */
    @Multipart
    @POST("orders/{order_id}/prescription")
    suspend fun uploadPrescription(
        @Path("order_id") orderId: String,
        @Part prescription: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody> = emptyMap()
    ): Response<ApiEnvelope<OrderDto>>
}
