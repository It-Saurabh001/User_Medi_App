package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val contex = LocalContext.current
//    var response = viewModel.createUserState.value
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val pinCode = remember { mutableStateOf("") }
    val context = LocalContext.current
    Scaffold(modifier = Modifier) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Name
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Name")
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

            //Phone Number
            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text("+91 Phone Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone")
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
            Spacer(modifier = Modifier.height(16.dp))
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
           // Address
            OutlinedTextField(
                value = address.value,
                onValueChange = { address.value = it },
                label = { Text("Address") },
                leadingIcon = {
                    Icon(Icons.Default.Home, contentDescription = "Address")
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

            //Pincode
            OutlinedTextField(
                value = pinCode.value,
                onValueChange = { pinCode.value = it },
                label = { Text("PinCode") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "pinCode")
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

            Button(onClick = {
                if (password.value == confirmPassword.value) {
                    viewModel.createUser(
                        name.value,
                        password.value,
                        phoneNumber.value,
                        email.value,
                        pinCode.value,
                        address.value,
                        role = "user",
                    )                }
                else {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }


                Log.d("TAG", "SignUp message: ")
                Log.d("TAG", "SignUp status: ")
            }
            ) {
                Text(text = "Sign Up")
                Toast.makeText(contex, "Successfully SignedUp", Toast.LENGTH_SHORT).show()
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(16.dp))
            Text(text = "If u have an account click SignIn button")
            Button(onClick = { navController.navigate(Routes.SignInRoutes.route) }) {
                Text(text = "Sign In")
            }
        }
    }
}

