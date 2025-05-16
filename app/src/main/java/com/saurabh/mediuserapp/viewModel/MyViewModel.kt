package com.saurabh.mediuserapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.network.response.CreateUserResponse
import com.saurabh.mediuserapp.network.response.GetSpecificUserResponse
import com.saurabh.mediuserapp.repo.repository
import com.saurabh.mediuserapp.utils.LoginUserState
import com.saurabh.mediuserapp.utils.User
import com.saurabh.mediuserapp.utils.UserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
    val repository = repository()
    val createUserState = mutableStateOf<CreateUserResponse?>(null)

    private val _loginUserState = MutableStateFlow(LoginUserState())
    val loginUserState = _loginUserState.asStateFlow()

    private val _specificUserState = MutableStateFlow(UserState())
    val specificUserState = _specificUserState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    fun createUser(
        name: String,
        password: String,
        phoneNumber: String,
        email: String,
        pinCode: String,
        address: String
    ) {
        viewModelScope.launch (Dispatchers.IO){
            val response = repository.createUser(name,password,phoneNumber,email,pinCode,address)
            if (response.isSuccessful) {
                createUserState.value = response.body()
            } else {
                Log.e("API_ERROR", "CreateUser failed: ${response.errorBody()?.string()}")
            }
        }

    }
    fun loginUser(email: String, password: String){
        viewModelScope.launch (Dispatchers.IO){
            repository.loginUser(email,password).collect {
                // collect is using here because of Flow use
                when(it){
                    is ResultState.Loading ->{
                        _loginUserState.value = LoginUserState(isLoading = true)
                    }
                    is ResultState.Error->{
                        _loginUserState.value = LoginUserState(error = it.exception.message, isLoading = false)
                    }
                    is ResultState.Success->{
                        _loginUserState.value = LoginUserState(success = it.data, isLoading = false)

                    }
                }
            }
        }
    }

    fun getSpecificUser(userId: String){
        viewModelScope.launch (Dispatchers.IO){
            repository.getSpecificUser(userId).collect {
                when(it){
                    is ResultState.Loading->{
                        _specificUserState.value = UserState(isLoading = true)
                    }
                    is ResultState.Error->{
                        _specificUserState.value = UserState(error = it.exception.message, isLoading = false)
                        Log.d("TAG", "getSpecificUser:  viewmodel ${_specificUserState.value.error}")
                    }
                    is ResultState.Success->{
                        _specificUserState.value = UserState(success = it.data, isLoading = false)
                        Log.d("TAG", "getSpecificUser:  viewmodel ${_specificUserState.value.success!!.message}")
                    }

                }
            }

        }
    }



    fun getAllUsers(){
        viewModelScope.launch (Dispatchers.IO){
            val response = repository.getAllUsers()
            _users.value = response.body() ?: emptyList()
        }

    }
}