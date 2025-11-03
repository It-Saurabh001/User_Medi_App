package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediuserapp.ui.nav.Routes

@Preview
@Composable
private fun sldgsd() {
    ProfileScreen(navController = rememberNavController())

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController : NavController) {
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier.fillMaxWidth(),

        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(

                title = { Text("Profile",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {

                }


            )
        }
    ) { innerpadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerpadding)
            .verticalScroll(scrollState)){
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp),
                thickness = 2.dp
            )
            Spacer(modifier = Modifier.height(4.dp))




        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center)
        {
            Column (
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                    Box(
                    modifier = Modifier.size(120.dp).padding(9.dp)

                        .border(3.dp, Color(0xFFE0E0E0), CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFADADAD)),
                    contentAlignment = Alignment.Center,
                ){
                    Text(
                        text = "S",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(text = "citizen23", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Citizen", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)


            }
            Spacer(modifier = Modifier.height(50.dp))



            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                colors = CardDefaults.cardColors(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 25.dp, bottom = 5.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Email", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "citizen23@gmail.com", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 2.dp
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Phone", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "+91 7895862545", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 2.dp
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Address", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Jankipuram, Lucknow, Uttar Pradesh", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 2.dp
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 25.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row (modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically){

                        Column(
                            modifier = Modifier.fillMaxWidth(0.7f)
                            ,
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        )  {
                            Text(text = "Aadhar Number", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "5245 5623 8545 6356", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        }

                        Surface (modifier = Modifier.fillMaxWidth()
                            .clickable(enabled = true, onClick = {navController.navigate(Routes.ProfileRoutes.route)}),
                            color = Color.White){
                            Box(modifier = Modifier.fillMaxWidth()
                                .clip(shape = RoundedCornerShape(8.dp))
                                .background(Color.Green),
                                contentAlignment = Alignment.Center){
                                Text(text = "Verify",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3),
                                    )



                            }


                        }
                    }


                }

            }
            Spacer(modifier = Modifier.height(30.dp))
            // Login Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }




        }
    }

}