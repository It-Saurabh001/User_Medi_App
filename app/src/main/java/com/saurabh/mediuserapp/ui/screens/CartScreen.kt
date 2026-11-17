package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediuserapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.DetailCard
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.QuantityStepper
import com.saurabh.mediuserapp.ui.components.SaveBar
import com.saurabh.mediuserapp.ui.components.ShadeButton
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.utils.screensState.CartItem
import com.saurabh.mediuserapp.viewModel.MyViewModel

/** Cart tab: lines with quantity stepper and remove, total, proceed to checkout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: MyViewModel, navController: NavController) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val checkout by viewModel.checkoutState.collectAsStateWithLifecycle()
    val total = cart.sumOf { it.price * it.quantity }

    AppScreenScaffold(
        title = "Cart",
        bottomBar = {
            if (cart.isNotEmpty()) SaveBar(
                text = "Checkout · ${total.inr()}",
                loading = false,
                onClick = { navController.navigate(Routes.CheckoutRoutes) { launchSingleTop = true } }
            )
        }
    ) { inner ->
        if (cart.isEmpty()) {
            EmptyState(
                title = "Your cart is empty",
                message = "Add medicines from the product page to order several at once.",
                modifier = Modifier.padding(inner),
                action = {
                    ShadeButton("Browse medicines", {
                        navController.navigate(Routes.ProductRoutes) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
            )
            return@AppScreenScaffold
        }
        AdaptiveContent(Modifier.padding(inner), maxWidth = AdaptiveWidths.Form) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            ) {
                itemsIndexed(cart, key = { _, it -> it.productId }) { index, item ->
                    CartLine(
                        item = item,
                        error = checkout.itemErrors[index],
                        onQuantity = { q -> viewModel.updateCart(item.productId) { it.copy(quantity = q) } },
                        onRemove = { viewModel.removeFromCart(item.productId) },
                        onOpen = { navController.navigate(Routes.SpecificProductRoutes(item.productId)) { launchSingleTop = true } }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLine(item: CartItem, error: String?, onQuantity: (Int) -> Unit, onRemove: () -> Unit, onOpen: () -> Unit) {
    DetailCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            ProductImage(item.imageUrl, item.dosageForm, size = 64.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text("${item.price.inr()} each", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.needsPrescription) ToneChip("Rx Required", Tone.Info)
            }
            TileIconButton(Icons.Outlined.Delete, "Remove ${item.name}", onRemove)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            QuantityStepper(value = item.quantity, onValueChange = onQuantity, min = 1, max = item.maxQuantity)
            Text((item.price * item.quantity).inr(), style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
        if (error != null) Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        com.saurabh.mediuserapp.ui.components.LinkButton("View product", onOpen)
    }
}
