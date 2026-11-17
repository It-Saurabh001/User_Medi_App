package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.repo.OrderFilter
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.ChipLabel
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.GsFilterChip
import com.saurabh.mediuserapp.ui.components.OrderCard
import com.saurabh.mediuserapp.ui.components.PagedLazyColumn
import com.saurabh.mediuserapp.ui.components.SkeletonList
import com.saurabh.mediuserapp.ui.components.label
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel

private val STATUS_FILTERS = listOf(null, OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.CANCELLED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.userOrdersState.collectAsStateWithLifecycle()
    val filter by viewModel.orderFilter.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getUserOrders(force = false) }
    val items = state.success?.items

    AppScreenScaffold(title = "My orders", scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && items != null,
            onRefresh = { viewModel.getUserOrders(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreOrders,
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(STATUS_FILTERS, key = { it?.name ?: "ALL" }) { s ->
                        GsFilterChip(
                            selected = filter.status == s,
                            onClick = { if (filter.status != s) viewModel.getUserOrders(OrderFilter(status = s)) },
                            label = { ChipLabel(s?.label() ?: "All") }
                        )
                    }
                }
            }
            when {
                items == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getUserOrders(force = true) })
                }
                items == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6, modifier = Modifier.padding(0.dp)) }
                items.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(
                        title = if (filter.status == null) "No orders yet" else "No ${filter.status?.label()?.lowercase()} orders",
                        message = if (filter.status == null) "Orders you place appear here." else "Try another status."
                    )
                }
                else -> items(items, key = { it.orderId }, contentType = { "order" }) { o ->
                    OrderCard(order = o, onClick = {
                        navController.navigate(Routes.OrderDetailRoutes(o.orderId)) { launchSingleTop = true }
                    })
                }
            }
        }
    }
}
