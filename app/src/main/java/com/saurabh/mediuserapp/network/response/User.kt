package com.saurabh.mediuserapp.network.response

import com.google.gson.annotations.SerializedName


data class User(
    val id: Int? = null,
    val user_id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val date_of_account_creation: String? = null,
    val address: String? = null,
    val email: String? = null,
    val phone_number: String? = null,
    val pin_code: String? = null,
    val password: String? = null,
    @SerializedName("isApproved")
    val _isApproved: Any? = null,
    @SerializedName("block")
    val _block: Any? = null
){
    val isApproved: Boolean         // This is a custom getter to handle both Boolean and Number types of approval
        get() = when(_isApproved){
            is Boolean -> _isApproved as Boolean
            is Number -> (_isApproved as Number).toInt() == 1
            else -> false
        }
    val block: Boolean
        get() = when(_block){
            is Boolean -> _block as Boolean
            is Number -> (_block as Number).toInt() == 1
            else -> false
        }
}