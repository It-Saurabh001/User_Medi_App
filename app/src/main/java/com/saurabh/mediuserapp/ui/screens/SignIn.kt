package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    val state = viewModel.loginUserState.collectAsState()
    val userState = viewModel.specificUserState.collectAsState()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    when{
        state.value.isLoading ->{
            Scaffold { innerpadding->
                Column (modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    CircularProgressIndicator()
                }
            }
        }
        state.value.error != null ->{
            Scaffold { innerpadding->
                Column (modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = state.value.error.toString())
                }
            }
        }
        state.value.success != null->{
            LaunchedEffect(state.value.success) {
                Log.d("TAG", "SignIn:  navigation to waiting screen ${state.value.success!!.message}")
                navController.navigate(Routes.WaitingRoutes(state.value.success!!.message))

            }
//            WaitingScreen(state.value.success!!.message,viewModel)
//            Log.d("TAG", "SignIn: ${state.value.success!!.message}")

        }
    }



    Scaffold (modifier = Modifier.fillMaxSize()){ innerpadding->
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(innerpadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally){
            OutlinedTextField(value = email.value, onValueChange = {email.value = it}, label = { Text(text = "Email") } )
            OutlinedTextField(value = password.value, onValueChange = {password.value = it}, label = { Text(text = "Password") } )

            Button(onClick = {viewModel.loginUser(email.value, password.value)}) {
                Text(text = "Sign In")
            }
        }
    }

}