package com.saurabh.mediuserapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.saurabh.mediuserapp.ui.nav.NavApp
import com.saurabh.mediuserapp.ui.theme.MediUserAppTheme
import com.saurabh.mediuserapp.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModels<MyViewModel>()
            MediUserAppTheme {
                NavApp(viewModel.value)
            }
        }
    }
}


