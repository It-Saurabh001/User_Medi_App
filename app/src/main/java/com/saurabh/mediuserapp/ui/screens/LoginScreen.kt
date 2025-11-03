package com.saurabh.mediuserapp.ui.screens

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediuserapp.ui.nav.Routes


@Preview
@Composable
private fun asgalsg() {
    val navController = rememberNavController()
    LoginScreen(navController)
    
}


// Login/Signup Page with Scaffold
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isCitizenEnable by  remember { mutableStateOf(false) }
    var isOfficerEnable by  remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {




                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter your phone number and password.",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center){
                            Button(onClick = {},
                                modifier = Modifier.fillMaxWidth(0.5f).padding(start = 8.dp, end =  8.dp),
                                enabled = isCitizenEnable,
                                shape = RoundedCornerShape(25),
                                colors = ButtonDefaults.buttonColors(Color.White)) {
                                Text(text = "Citizen",
                                    color = if (isCitizenEnable) Color(0xFF2196F3) else Color.Black
                                    )

                            }
//                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {},
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end =  8.dp),
                                enabled = isOfficerEnable,
                                shape = RoundedCornerShape(25),
                                colors = ButtonDefaults.buttonColors(Color.White)) {
                                Text(text = "Officer",
                                    color = if (isOfficerEnable) Color(0xFF2196F3) else Color.Black
                                    )

                            }

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Number Field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
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
                            value = password,
                            onValueChange = { password = it },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Login Button
                        Button(
                            onClick = { navController.navigate(Routes.HomeRoutes.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Log In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign Up Text
                        Row(modifier = Modifier,
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Don't have an account? ",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                            TextButton(
                                onClick = { navController.navigate(Routes.SignUpRoutes.route) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Sign Up",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }



            }
        }
    )
}


