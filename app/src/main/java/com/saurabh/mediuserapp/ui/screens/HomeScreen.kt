package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


@Preview
@Composable
private fun prever() {
    val navController = rememberNavController()
    HomeScreen(navController)
    
}

// 1. Home Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = "Citizen Logo",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Citizen", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome, Saurabh",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Report issues in your community",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 1.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
// Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    label = { Text("Search location", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    leadingIcon = {Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray)},
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Map Area (Simulated)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Color(0xFF4CAF50),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    // Map pins simulation
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = Color.Blue,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(32.dp)
                            .size(24.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = Color.Green,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(48.dp)
                            .size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Report Button
                Button(
                    onClick = { navController.navigate("report_issue") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Report",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Report an Issue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                // Recent Reports Section
                Text(
                    text = "Recent Reports",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Recent Reports List
                RecentReportItem("Pothole on Main Street", "Resolved", Color(0xFF4CAF50))
                RecentReportItem("Streetlight Outage", "In Progress", Color(0xFF2196F3))

            }

            }
    }
}

@Composable
fun RecentReportItem(title: String, status: String, statusColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF26FDC
            )
        ),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        border = CardDefaults.outlinedCardBorder(true)

    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = status,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.Gray
            )
        }
    }
}