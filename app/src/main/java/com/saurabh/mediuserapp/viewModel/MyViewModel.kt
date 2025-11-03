package com.saurabh.mediuserapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediuserapp.common.ResultState
import com.saurabh.mediuserapp.repo.repository
import com.saurabh.mediuserapp.utils.CreateUserState
import com.saurabh.mediuserapp.utils.LoginUserState
import com.saurabh.mediuserapp.utils.User
import com.saurabh.mediuserapp.utils.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MyViewModel @Inject constructor(private val repository: repository) : ViewModel() {
//    val repository = repository()
    private val _createUserState = MutableStateFlow(CreateUserState())
    val createUserState = _createUserState.asStateFlow()

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
        address: String,
        role: String
    ) {
        if (_createUserState.value.success != null ) {
            Log.d("TAG", "createUser: User already created")
            return
        }

        viewModelScope.launch (Dispatchers.IO){
            _createUserState.value = CreateUserState(isLoading = true)
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
                        Log.d("TAG", "getSpecificUser:  viewmodel error ${_specificUserState.value.error}")
                    }
                    is ResultState.Success->{
                        _specificUserState.value = UserState(success = it.data, isLoading = false)
                        Log.d("TAG", "getSpecificUser:  viewmodel success ${_specificUserState.value.success!!.message}")
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