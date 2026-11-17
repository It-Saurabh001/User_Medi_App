package com.saurabh.mediuserapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** Shown after 201 from POST orders (PlaceOrder already popped). Back = Products. */
@Composable
fun OrderPlacedScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.createOrderState.collectAsStateWithLifecycle()
    val order = state.success?.takeIf { it.orderId == orderId }
    DisposableEffect(Unit) { onDispose { viewModel.resetCreateOrderState() } }

    fun continueShopping() {
        if (!navController.popBackStack(Routes.ProductRoutes, inclusive = false)) {
            navController.navigate(Routes.ProductRoutes) { popUpTo(0) { inclusive = true } }
        }
    }
    BackHandler { continueShopping() }

    val details = order?.let {
        "${it.productName.capitalizeEachWord()} · ${it.quantity} × ${it.price.inr()} = ${it.totalAmount.inr()}. "
    }.orEmpty()
    StatusMessageScreen(
        icon = Icons.Outlined.CheckCircle,
        tone = Tone.Success,
        title = "Order placed",
        message = "${details}Order $orderId is pending approval by the pharmacy.",
        actionText = "View order",
        onAction = {
            navController.navigate(Routes.OrderDetailRoutes(orderId)) {
                popUpTo(Routes.OrderPlacedRoutes(orderId)) { inclusive = true }
                launchSingleTop = true
            }
        },
        secondaryText = "Continue shopping",
        onSecondary = ::continueShopping
    )
}
