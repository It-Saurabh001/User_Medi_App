package com.saurabh.mediuserapp.ui.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saurabh.mediuserapp.ui.components.shadeColor
import com.saurabh.mediuserapp.ui.screens.AccountBlockedScreen
import com.saurabh.mediuserapp.ui.screens.BarcodeScannerScreen
import com.saurabh.mediuserapp.ui.screens.ChangeCredentialsScreen
import com.saurabh.mediuserapp.ui.screens.EditProfileScreen
import com.saurabh.mediuserapp.ui.screens.InvoiceScreen
import com.saurabh.mediuserapp.ui.screens.OrderDetailScreen
import com.saurabh.mediuserapp.ui.screens.OrderPlacedScreen
import com.saurabh.mediuserapp.ui.screens.PrescriptionViewerScreen
import com.saurabh.mediuserapp.ui.screens.UploadPrescriptionScreen
import com.saurabh.mediuserapp.ui.screens.PlaceOrderScreen
import com.saurabh.mediuserapp.ui.screens.ForgotPasswordOtpScreen
import com.saurabh.mediuserapp.ui.screens.ProductDetailScreen
import com.saurabh.mediuserapp.ui.screens.UpgradeRequiredScreen
import com.saurabh.mediuserapp.ui.screens.ForgotPasswordScreen
import com.saurabh.mediuserapp.ui.screens.HistoryScreen
import com.saurabh.mediuserapp.ui.screens.OrdersScreen
import com.saurabh.mediuserapp.ui.screens.OtpScreen
import com.saurabh.mediuserapp.ui.screens.ProductScreen
import com.saurabh.mediuserapp.ui.screens.ProfileScreen
import com.saurabh.mediuserapp.ui.screens.ResetPasswordScreen
import com.saurabh.mediuserapp.ui.screens.SettingsAboutScreen
import com.saurabh.mediuserapp.ui.screens.SignIn
import com.saurabh.mediuserapp.ui.screens.SignUp
import com.saurabh.mediuserapp.ui.screens.SplashScreen
import com.saurabh.mediuserapp.ui.screens.WaitingScreen
import com.saurabh.mediuserapp.ui.theme.Blue100
import com.saurabh.mediuserapp.ui.theme.Blue50
import com.saurabh.mediuserapp.ui.theme.Shade
import com.saurabh.mediuserapp.ui.theme.Shades
import com.saurabh.mediuserapp.ui.theme.Transparent
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlin.reflect.KClass

private data class TopLevelTab(
    val label: String,
    val route: Any,
    val routeClass: KClass<*>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topLevelTabs = listOf(
    TopLevelTab("Home", Routes.ProductRoutes, Routes.ProductRoutes::class, Icons.Filled.Home, Icons.Outlined.Home),
    TopLevelTab("Orders", Routes.OrdersRoutes, Routes.OrdersRoutes::class, Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    TopLevelTab("History", Routes.HistoryRoutes, Routes.HistoryRoutes::class, Icons.Filled.History, Icons.Outlined.History),
    TopLevelTab("Profile", Routes.ProfileRoutes, Routes.ProfileRoutes::class, Icons.Filled.Person, Icons.Outlined.Person),
)

private val authRouteClasses: List<KClass<*>> = listOf(
    Routes.SplashRoutes::class, Routes.SignInRoutes::class, Routes.SignUpRoutes::class,
    Routes.VerifyOtpRoutes::class, Routes.WaitingRoutes::class, Routes.ForgotPasswordRoutes::class,
    Routes.ForgotPasswordOtpRoutes::class, Routes.ResetPasswordRoutes::class,
    Routes.UpgradeRequiredRoutes::class, Routes.AccountBlockedRoutes::class,
)

private fun NavDestination?.isAny(classes: List<KClass<*>>): Boolean =
    this != null && classes.any { hasRoute(it) }

private fun NavDestination?.tabIndex(): Int =
    if (this == null) -1 else topLevelTabs.indexOfFirst { hasRoute(it.routeClass) }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.tabIndex() >= 0 && targetState.destination.tabIndex() >= 0

@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val selectedTab = destination.tabIndex()
    val isAuthRoute = destination == null || destination.isAny(authRouteClasses)
    val loggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val upgradeRequired by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    val accountBlocked by viewModel.accountBlocked.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short) }
    }

    LaunchedEffect(upgradeRequired, destination) {
        if (upgradeRequired != null && destination != null && !destination.hasRoute(Routes.UpgradeRequiredRoutes::class)) {
            navController.navigate(Routes.UpgradeRequiredRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(accountBlocked, upgradeRequired, destination) {
        if (accountBlocked && upgradeRequired == null && destination != null &&
            !destination.hasRoute(Routes.AccountBlockedRoutes::class)
        ) {
            navController.navigate(Routes.AccountBlockedRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(loggedIn, isAuthRoute, accountBlocked, upgradeRequired) {
        if (!loggedIn && !isAuthRoute && !accountBlocked && upgradeRequired == null) {
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedTab >= 0) {
                AppNavigationBar(selected = selectedTab) { index ->
                    navController.navigate(topLevelTabs[index].route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SplashRoutes,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding())),
            enterTransition = {
                if (isTabSwitch()) fadeIn(tween(200))
                else slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300))
            },
            exitTransition = {
                if (isTabSwitch()) fadeOut(tween(200))
                else slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(300), targetAlpha = 0.85f)
            },
            popEnterTransition = {
                if (isTabSwitch()) fadeIn(tween(200))
                else slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(280))
            },
            popExitTransition = {
                if (isTabSwitch()) fadeOut(tween(200))
                else slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(280))
            },
        ) {
            composable<Routes.SplashRoutes> { SplashScreen(navController, viewModel) }
            composable<Routes.SignInRoutes> { SignIn(viewModel, navController) }
            composable<Routes.SignUpRoutes> { SignUp(viewModel, navController) }
            composable<Routes.VerifyOtpRoutes> { entry ->
                OtpScreen(entry.toRoute<Routes.VerifyOtpRoutes>().accountId, viewModel, navController)
            }
            composable<Routes.WaitingRoutes> { entry ->
                WaitingScreen(entry.toRoute<Routes.WaitingRoutes>().email.orEmpty(), navController)
            }
            composable<Routes.ForgotPasswordRoutes> { ForgotPasswordScreen(viewModel, navController) }
            composable<Routes.ForgotPasswordOtpRoutes> { entry ->
                ForgotPasswordOtpScreen(entry.toRoute<Routes.ForgotPasswordOtpRoutes>().email, viewModel, navController)
            }
            composable<Routes.ResetPasswordRoutes> { entry ->
                val r = entry.toRoute<Routes.ResetPasswordRoutes>()
                ResetPasswordScreen(r.email, r.otp, viewModel, navController)
            }

            composable<Routes.UpgradeRequiredRoutes> { UpgradeRequiredScreen(viewModel) }
            composable<Routes.AccountBlockedRoutes> { AccountBlockedScreen(viewModel, navController) }

            composable<Routes.ProductRoutes> { ProductScreen(viewModel, navController) }
            composable<Routes.SpecificProductRoutes> { entry ->
                ProductDetailScreen(entry.toRoute<Routes.SpecificProductRoutes>().productId, viewModel, navController)
            }
            composable<Routes.BarcodeScannerRoutes> { BarcodeScannerScreen(viewModel, navController) }
            composable<Routes.PlaceOrderRoutes> { entry ->
                PlaceOrderScreen(entry.toRoute<Routes.PlaceOrderRoutes>().productId, viewModel, navController)
            }
            composable<Routes.OrderPlacedRoutes> { entry ->
                OrderPlacedScreen(entry.toRoute<Routes.OrderPlacedRoutes>().orderId, viewModel, navController)
            }
            composable<Routes.OrdersRoutes> { OrdersScreen(viewModel, navController) }
            composable<Routes.OrderDetailRoutes> { entry ->
                OrderDetailScreen(entry.toRoute<Routes.OrderDetailRoutes>().orderId, viewModel, navController)
            }
            composable<Routes.UploadPrescriptionRoutes> { entry ->
                UploadPrescriptionScreen(entry.toRoute<Routes.UploadPrescriptionRoutes>().orderId, viewModel, navController)
            }
            composable<Routes.PrescriptionViewerRoutes> { entry ->
                PrescriptionViewerScreen(entry.toRoute<Routes.PrescriptionViewerRoutes>().orderId, viewModel, navController)
            }
            composable<Routes.InvoiceRoutes> { entry ->
                InvoiceScreen(entry.toRoute<Routes.InvoiceRoutes>().orderId, viewModel, navController)
            }
            composable<Routes.HistoryRoutes> { HistoryScreen(viewModel, navController) }
            composable<Routes.ProfileRoutes> { ProfileScreen(viewModel, navController) }
            composable<Routes.EditProfileRoutes> { EditProfileScreen(viewModel, navController) }
            composable<Routes.ChangeCredentialsRoutes> { ChangeCredentialsScreen(viewModel, navController) }
            composable<Routes.SettingsRoutes> { SettingsAboutScreen(viewModel, navController) }
        }
    }
}

@Composable
private fun AppNavigationBar(selected: Int, onItemSelected: (Int) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
        ) {
            topLevelTabs.forEachIndexed { index, tab ->
                NavBarItem(
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedIcon = tab.selectedIcon,
                    unselectedIcon = tab.unselectedIcon,
                    label = tab.label,
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
) {
    val source = remember { MutableInteractionSource() }
    val shade = if (selected) Shades.BlueChipSelected else Shade(Transparent, Blue50, Blue100)
    val bg by source.shadeColor(shade, "navItem")
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .weight(1f)
            .height(80.dp)
            .clickable(interactionSource = source, indication = null, role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(width = 64.dp, height = 32.dp).clip(MaterialTheme.shapes.extraLarge).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(selected, animationSpec = tween(110), label = "navIcon") { sel ->
                Icon(if (sel) selectedIcon else unselectedIcon, contentDescription = null, tint = fg)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label, style = MaterialTheme.typography.labelMedium, color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else null,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}
