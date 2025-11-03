package com.saurabh.mediuserapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.network.response.User
import com.saurabh.mediuserapp.ui.components.SpecificUser
import com.saurabh.mediuserapp.viewModel.MyViewModel

@Composable
fun WaitingScreen(userId: String, viewModel: MyViewModel) {
    val userid = remember { mutableStateOf("") }
    val userState = viewModel.specificUserState.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.getSpecificUser(userId)
    }
    LaunchedEffect(userState) {
        viewModel.getSpecificUser(userId)
    }
    Scaffold (modifier = Modifier.fillMaxSize()) { innerpadding ->
        when {
            userState.value.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "This is loading screen")
                    Text(text = userId)

                    Log.d("TAG", "WaitingScreen: success")

                    CircularProgressIndicator()

                }
            }

            userState.value.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = userState.value.error.toString())
                    Text(text = "error screen")


                }
            }

            userState.value.success != null -> {
                if (userState.value.success!!.user.isApproved == false){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerpadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                    Text(text = "This is waiting screen ")
                        Text(text = "Wait for admin approval :) ")
                        if (userId != null) {
                            Text(text = userId)
                        }
                        Log.d("TAG", "WaitingScreen: success")

                        Button(onClick = {
                            viewModel.getSpecificUser(userId)           // this but rerun api and refresh the api
                        }) {
                            Text(text = "Refresh")
                        }
                    }
                }else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerpadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
//                    Text(text = "This is waiting success screen ")
//                    Text(text = "${userId}")
                        Log.d("TAG", "WaitingScreen: success")

//                    HorizontalDivider()
                        SpecificUser(userState.value.success!!.user)
                    }
                }

            }

        }

    }
}



