package com.saurabh.mediuserapp.ui.nav

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saurabh.mediuserapp.ui.screens.SignIn
import com.saurabh.mediuserapp.ui.screens.SignUp
import com.saurabh.mediuserapp.ui.screens.WaitingScreen
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.launch

@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current


    val bottomNavItem = listOf(
        BottomNavigationItem("Dashboard", Icons.Filled.Home),
        BottomNavigationItem("Add Product", Icons.Filled.Add),
        BottomNavigationItem("Orders", Icons.Filled.ShoppingCart),
        BottomNavigationItem("History", Icons.Filled.Menu)
    )
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {bottomNavItem.size}
    )
    LaunchedEffect(pagerState.currentPage) {
        selected = pagerState.currentPage
    }

    Scaffold (
//        topBar = getTopBarForRoute(currentRoute,navController, viewModel),
//            modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomNavItem.forEachIndexed { index, bottomNavItem ->
                    NavigationBarItem(
                        alwaysShowLabel = true,
                        selected = selected == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            selected = index
                            when (selected) {
                                0 -> navController.navigate(Routes.HomeRoutes())
                                1 -> navController.navigate(Routes.ProductRoutes())
                                2 -> navController.navigate(Routes.OrdersRoutes())
                                3 -> navController.navigate(Routes.HistoryRoutes())
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = bottomNavItem.icon,
                                contentDescription = bottomNavItem.name
                            )
                        },
                        label = { Text(text = bottomNavItem.name) }
                    )
                }
            }
        }
    ){innerpadding->



    NavHost(
        navController = navController,
        startDestination = Routes.SignUpRoutes,
        modifier = Modifier.padding(innerpadding)
    ) {
        composable<Routes.SignUpRoutes> {
            SignUp(viewModel,navController)
        }
        composable<Routes.SignInRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.HomeRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.SplashRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.HistoryRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.ProfileRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.OtpRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.OtpRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.OtpRequestRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.ProductRoutes> {
            SignIn(viewModel,navController)
        }
        composable<Routes.OrdersRoutes> {
            SignIn(viewModel,navController)
        }
        composable <Routes.WaitingRoutes>{
            val data = it.toRoute<Routes.WaitingRoutes>()
            Log.d("TAG", "NavApp: userId = ${data.userId}")
            val userId = data.userId
            Log.d("TAG", "NavApp: userId = $userId")

            if (!userId.isNullOrEmpty()) {
                WaitingScreen(userId, viewModel)
            } else {
                // Option 1: Show loading or fallback UI
                navController.popBackStack()
                Toast.makeText(context, "User not found", Toast.LENGTH_LONG).show()

            }
        }
        composable <Routes.SpecificProductRoutes>{
            val data = it.toRoute<Routes.SpecificProductRoutes>()
            Log.d("TAG", "NavApp: userId = ${data.productId}")
//            WaitingScreen(data.productId, viewModel)
        }
        composable <Routes.EachUserOrderRoutes>{
            val data = it.toRoute<Routes.EachUserOrderRoutes>()
            Log.d("TAG", "NavApp: userId = ${data.userId}")
//            WaitingScreen(data.userId, viewModel)
        }


    }
}

}
data class BottomNavigationItem(val name: String, val icon : ImageVector)
