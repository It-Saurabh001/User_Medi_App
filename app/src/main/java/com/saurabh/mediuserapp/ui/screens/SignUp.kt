package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val contex = LocalContext.current
    var response = viewModel.createUserState.value
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val pinCode = remember { mutableStateOf("") }







    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text(text = "Name") })
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text(text = "password") })
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = "email") })
            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text(text = "phoneNumber") })
            OutlinedTextField(
                value = address.value,
                onValueChange = { address.value = it },
                label = { Text(text = "address") })
            OutlinedTextField(
                value = pinCode.value,
                onValueChange = { pinCode.value = it },
                label = { Text(text = "pinCode") })

            Button(onClick = {
                viewModel.createUser(
                    name.value,
                    password.value,
                    phoneNumber.value,
                    email.value,
                    pinCode.value,
                    address.value,
                )
                Toast.makeText(contex, "${response?.status}", Toast.LENGTH_SHORT).show()

                Log.d("TAG", "SignUp message: ${response?.message}")
                Log.d("TAG", "SignUp status: ${response?.status}")
            }
            ) {
                Text(text = "Sign Up")

            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(16.dp))
            Text(text = "If u have an account click SignIn button")
            Button(onClick = { navController.navigate(Routes.SignInRoutes) }) {
                Text(text = "Sign In")
            }
        }
    }
}

