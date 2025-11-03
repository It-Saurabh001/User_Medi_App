package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    val state = viewModel.loginUserState.collectAsState()
    val userState = viewModel.specificUserState.collectAsState()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val context  = LocalContext.current

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
                    Toast.makeText(context, state.value.error.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        state.value.success != null->{
            LaunchedEffect(state.value.success) {
                Log.d("TAG", "SignIn:  navigation to waiting screen ${state.value.success!!.user_id}")
                navController.navigate(Routes.WaitingRoutes(state.value.success!!.user_id))

            }
//            WaitingScreen(state.value.success!!.message,viewModel)
//            Log.d("TAG", "SignIn: ${state.value.success!!.message}")

        }
    }



    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
    ){ innerpadding->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Password Field
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Login Button

            Button(onClick = {
                viewModel.loginUser(email.value, password.value)}
            ) {
                Text(text = "Sign In")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {}) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF2196F3),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

}