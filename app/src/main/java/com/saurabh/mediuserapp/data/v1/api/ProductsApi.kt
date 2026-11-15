package com.saurabh.mediuserapp.data.v1.api

import com.saurabh.mediuserapp.data.v1.core.ApiEnvelope
import com.saurabh.mediuserapp.data.v1.core.PagedEnvelope
import com.saurabh.mediuserapp.data.v1.dto.ProductCategoryDto
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/** Page params via PageRequest.toQueryMap(). products sort: name|price|mrp|stock|created_at|updated_at. */
interface ProductsApi {
    @GET("products")
    suspend fun getProducts(
        @Query("q") q: String? = null,
        @Query("category") category: String? = null,
        @Query("schedule_type") scheduleType: String? = null,
        @Query("requires_prescription") requiresPrescription: Boolean? = null,
        @Query("in_stock") inStock: Boolean? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<ProductDto>>

    @GET("products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: String): Response<ApiEnvelope<ProductDto>>

    @GET("products/by-barcode/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<ApiEnvelope<ProductDto>>

    /** sort: name|product_count. */
    @GET("product-categories")
    suspend fun getCategories(
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<ProductCategoryDto>>
}
