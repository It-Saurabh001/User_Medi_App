package com.saurabh.mediuserapp.ui.nav

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saurabh.mediuserapp.ui.screens.*
import com.saurabh.mediuserapp.viewModel.MyViewModel

import com.saurabh.mediuserapp.utils.AppLogger
import com.saurabh.mediuserapp.utils.logAction

/**
 * Root navigation host for MediUserApp.
 *
 * ─── Architecture ──────────────────────────────────────────────────────────
 *
 * startDestination is ALWAYS [Routes.SplashRoutes] — fixed and never changes.
 * The SplashScreen reads the token and decides which graph to enter:
 *   • Token found  → ProductRoutes  (main app graph)
 *   • No token     → SignInRoutes   (auth graph)
 *
 * This avoids the anti-pattern of a dynamic startDestination which causes
 * NavHost recomposition issues and flickering.
 *
 * ─── Back Stack Rules ──────────────────────────────────────────────────────
 * • Splash  → popped off immediately after navigation
 * • Login   → after auth success, entire auth graph popped (can't go back)
 * • Logout  → entire main graph popped, lands on SignIn (can't go back)
 */
@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var selected by rememberSaveable { mutableIntStateOf(0) }

    // Bottom bar is only visible on the four main authenticated screens
    val showBottomBar = currentRoute?.let { route ->
        listOf(
            Routes.ProductRoutes::class.qualifiedName,
            Routes.ProfileRoutes::class.qualifiedName,
            Routes.OrdersRoutes::class.qualifiedName,
            Routes.HistoryRoutes::class.qualifiedName
        ).any { route.startsWith(it ?: "") }
    } == true

    val bottomNavItems = listOf(
        BottomNavItem("Home",    Icons.Filled.Home,         0),
        BottomNavItem("Orders",  Icons.Filled.ShoppingCart, 1),
        BottomNavItem("History", Icons.Filled.History,      2),
        BottomNavItem("Profile", Icons.Filled.Person,       3)
    )

    // Keep pager in sync with bottom bar
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { bottomNavItems.size })
    LaunchedEffect(pagerState.currentPage) { selected = pagerState.currentPage }

    // Track composition
    SideEffect { AppLogger.v(AppLogger.TAG_NAV, "NavApp recomposed") }
    LaunchedEffect(Unit) { AppLogger.i(AppLogger.TAG_NAV, "NavApp ENTERED composition") }

    AppLogger.d(AppLogger.TAG_NAV, "NavApp status: currentRoute=$currentRoute, showBottomBar=$showBottomBar")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = selected == item.index,
                            onClick = {
                                selected = item.index
                                AppLogger.d(AppLogger.TAG_NAV, "BottomNav tapped: ${item.label}").logAction("Navigation Action")
                                when (item.index) {
                                    0 -> navController.navigate(Routes.ProductRoutes) {
                                        popUpTo(Routes.ProductRoutes) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    1 -> navController.navigate(Routes.OrdersRoutes) {
                                        popUpTo(Routes.ProductRoutes)
                                        launchSingleTop = true
                                    }
                                    2 -> navController.navigate(Routes.HistoryRoutes) {
                                        popUpTo(Routes.ProductRoutes)
                                        launchSingleTop = true
                                    }
                                    3 -> navController.navigate(Routes.ProfileRoutes) {
                                        popUpTo(Routes.ProductRoutes)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
            val NAV_TAG = "NavHost"
        NavHost(
            navController = navController,
            // ✅ FIXED: startDestination is ALWAYS SplashRoutes — never dynamic.
            // SplashScreen handles the auth check and navigates accordingly.
            startDestination = Routes.SplashRoutes,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ─────────────────────────────────────────────────────────────────
            // AUTH ENTRY POINT — Auth check happens here, ALWAYS first
            // ─────────────────────────────────────────────────────────────────
            composable<Routes.SplashRoutes> {
                Log.d(NAV_TAG, "Destination: SplashRoutes")
                SplashScreen(navController, viewModel)
            }

            // ─────────────────────────────────────────────────────────────────
            // AUTH GRAPH
            // ─────────────────────────────────────────────────────────────────

            composable<Routes.SignInRoutes> {
                Log.d(NAV_TAG, "Destination: SignInRoutes")
                SignIn(viewModel, navController)
            }

            composable<Routes.SignUpRoutes> {
                Log.d(NAV_TAG, "Destination: SignUpRoutes")
                SignUp(viewModel, navController)
            }

            // Login OTP verification — receives userId from LoginUserResponse
            composable<Routes.VerifyOtpRoutes> { backStackEntry ->
                val userId = backStackEntry.toRoute<Routes.VerifyOtpRoutes>().userId.orEmpty()
                Log.d(NAV_TAG, "Destination: VerifyOtpRoutes, userId=$userId")
                OtpScreen(userId, viewModel, navController)
            }

            // Forgot password — Step 1: enter email
            composable<Routes.ForgotPasswordRoutes> {
                Log.d(NAV_TAG, "Destination: ForgotPasswordRoutes")
                ForgotPasswordScreen(viewModel, navController)
            }

            // Forgot password — Step 2: enter OTP from email
            composable<Routes.ForgotPasswordOtpRoutes> { backStackEntry ->
                val userId = backStackEntry.toRoute<Routes.ForgotPasswordOtpRoutes>().userId
                Log.d(NAV_TAG, "Destination: ForgotPasswordOtpRoutes, userId=$userId")
                ForgotPasswordOtpScreen(userId, viewModel, navController)
            }

            // Forgot password — Step 3: enter new password
            composable<Routes.ResetPasswordRoutes> { backStackEntry ->
                val userId = backStackEntry.toRoute<Routes.ResetPasswordRoutes>().userId
                Log.d(NAV_TAG, "Destination: ResetPasswordRoutes, userId=$userId")
                ResetPasswordScreen(userId, viewModel, navController)
            }

            // ─────────────────────────────────────────────────────────────────
            // MAIN APP GRAPH (authenticated users only)
            // ─────────────────────────────────────────────────────────────────

            composable<Routes.ProductRoutes> {
                LaunchedEffect(Unit) { selected = 0 }
                Log.d(NAV_TAG, "Destination: ProductRoutes")
                ProductScreen(viewModel, navController)
            }

            composable<Routes.OrdersRoutes> {
                LaunchedEffect(Unit) { selected = 1 }
                Log.d(NAV_TAG, "Destination: OrdersRoutes")
                OrdersScreen(navController)
            }

            composable<Routes.HistoryRoutes> {
                LaunchedEffect(Unit) { selected = 2 }
                Log.d(NAV_TAG, "Destination: HistoryRoutes")
                HistoryScreen(navController)
            }

            composable<Routes.ProfileRoutes> {
                LaunchedEffect(Unit) { selected = 3 }
                Log.d(NAV_TAG, "Destination: ProfileRoutes")
                // Pass viewModel so ProfileScreen can trigger logout
                ProfileScreen(navController, viewModel)
            }
        }
    }
}

/** Data holder for bottom navigation items. */
data class BottomNavItem(val label: String, val icon: ImageVector, val index: Int)

// Keep the old name for any existing references
typealias BottomNavigationItem = BottomNavItem
