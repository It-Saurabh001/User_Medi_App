package com.saurabh.mediuserapp.repo

import android.util.Log
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.network.ApiProvider
import com.saurabh.mediuserapp.network.response.GetSpecificUserResponse
import com.saurabh.mediuserapp.network.response.LoginUserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class repository {

    // function name
    suspend fun createUser(
        name: String,
        password: String,
        phoneNumber: String,
        email: String,
        pinCode: String,
        address: String,
    ) = ApiProvider.providerApiServices()
        .createUser(name,password,phoneNumber,email,pinCode,address)


    suspend fun loginUser(
        email: String,
        password: String

    ) : Flow<ResultState<LoginUserResponse>> = flow {
        emit(ResultState.Loading)

        try {
            // will hit api
            val response = ApiProvider.providerApiServices().loginUser(email,password)
            if (response.isSuccessful){
                emit(ResultState.Success(response.body()!!))  // api services ke andar reponse le rkha hai aur yha response ko body chiye
            }
            else {
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }
    suspend fun getSpecificUser(userId:String): Flow<ResultState<GetSpecificUserResponse>> = flow {
        emit(ResultState.Loading)

        try{
            val response = ApiProvider.providerApiServices().getSpecificUser(userId)
            if (response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getSpecificUser repository: ${ResultState.Success(response.body()!!)}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
            }



        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getAllUsers() = ApiProvider.providerApiServices().getAllUsers()
}