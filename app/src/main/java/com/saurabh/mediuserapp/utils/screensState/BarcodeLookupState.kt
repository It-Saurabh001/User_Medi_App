package com.saurabh.mediuserapp.utils.screensState

import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.data.v1.error.AppError

/** GET products/by-barcode/{barcode} lookup for BarcodeScanner. */
sealed interface BarcodeLookupState {
    data object Idle : BarcodeLookupState
    data class Loading(val barcode: String) : BarcodeLookupState
    data class Found(val barcode: String, val product: ProductDto) : BarcodeLookupState
    data class NotFound(val barcode: String) : BarcodeLookupState
    data class Failed(val barcode: String, val message: String, val error: AppError?) : BarcodeLookupState
}
