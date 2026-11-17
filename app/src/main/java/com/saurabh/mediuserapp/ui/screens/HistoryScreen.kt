package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.repo.DateRange
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.CheckoutGroupRow
import com.saurabh.mediuserapp.ui.components.ChipLabel
import com.saurabh.mediuserapp.ui.components.groupedByCheckout
import com.saurabh.mediuserapp.ui.components.DateField
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.GsFilterChip
import com.saurabh.mediuserapp.ui.components.LinkButton
import com.saurabh.mediuserapp.ui.components.OrderCard
import com.saurabh.mediuserapp.ui.components.PagedLazyColumn
import com.saurabh.mediuserapp.ui.components.SkeletonList
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.viewModel.MyViewModel
import java.time.LocalDate

private val HISTORY_STATUSES = listOf(OrderStatus.SOLD, OrderStatus.RETURNED)

/** Completed purchases: GET orders?status=sold|returned with an optional date range. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.historyState.collectAsStateWithLifecycle()
    val filter by viewModel.historyFilter.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getHistoryOrders(force = false) }
    val items = state.success?.items
    var expanded by rememberSaveable { mutableStateOf(setOf<String>()) }
    val from = filter.dateRange?.from
    val to = filter.dateRange?.to
    val rangeError = if (from != null && to != null && from.isAfter(to)) "From date must be before To date" else null

    fun setRange(newFrom: LocalDate?, newTo: LocalDate?) {
        val range = if (newFrom == null && newTo == null) null else DateRange(newFrom, newTo)
        if (newFrom != null && newTo != null && newFrom.isAfter(newTo)) return
        viewModel.getHistoryOrders(filter.copy(dateRange = range))
    }

    AppScreenScaffold(title = "History", scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && items != null,
            onRefresh = { viewModel.getHistoryOrders(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreHistory,
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            item(key = "filters", contentType = "filters") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HISTORY_STATUSES.forEach { s ->
                            GsFilterChip(
                                selected = filter.status == s,
                                onClick = { if (filter.status != s) viewModel.getHistoryOrders(filter.copy(status = s)) },
                                label = { ChipLabel(if (s == OrderStatus.SOLD) "Purchased" else "Returned") }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        DateField(
                            label = "From", iso = from?.toString().orEmpty(),
                            onIsoChange = { setRange(LocalDate.parse(it), to) },
                            enabled = !state.isLoading, error = rangeError,
                            modifier = Modifier.weight(1f)
                        )
                        DateField(
                            label = "To", iso = to?.toString().orEmpty(),
                            onIsoChange = { setRange(from, LocalDate.parse(it)) },
                            enabled = !state.isLoading, error = null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (from != null || to != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            LinkButton(text = "Clear dates", onClick = { setRange(null, null) })
                        }
                    }
                }
            }
            when {
                items == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getHistoryOrders(force = true) })
                }
                items == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6, modifier = Modifier.padding(0.dp)) }
                items.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(
                        title = if (filter.status == OrderStatus.RETURNED) "No returned orders" else "No purchases yet",
                        message = if (filter.dateRange != null) "Try a different date range." else "Completed orders appear here with their invoice."
                    )
                }
                else -> {
                    val open: (com.saurabh.mediuserapp.data.v1.dto.OrderDto) -> Unit = { o ->
                        navController.navigate(Routes.OrderDetailRoutes(o.orderId)) { launchSingleTop = true }
                    }
                    items.groupedByCheckout().forEach { entry ->
                        if (entry.size == 1) {
                            val o = entry.first()
                            item(key = o.orderId, contentType = "order") { OrderCard(order = o, onClick = { open(o) }) }
                        } else {
                            val cid = entry.first().checkoutId.orEmpty()
                            item(key = "chk_$cid", contentType = "group") {
                                CheckoutGroupRow(entry, expanded = cid in expanded, onToggle = {
                                    expanded = if (cid in expanded) expanded - cid else expanded + cid
                                })
                            }
                            if (cid in expanded) items(entry, key = { "${cid}_${it.orderId}" }, contentType = { "order" }) { o ->
                                Box(Modifier.padding(start = 16.dp)) { OrderCard(order = o, onClick = { open(o) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}
