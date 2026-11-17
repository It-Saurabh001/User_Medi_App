package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.data.v1.core.OrderStatus
import com.saurabh.mediuserapp.data.v1.dto.OrderDto
import com.saurabh.mediuserapp.ui.theme.Transparent
import com.saurabh.mediuserapp.utils.capitalizeEachWord

/** Orders sharing a checkoutId become one group (list order kept); orders without one stay single. */
fun List<OrderDto>.groupedByCheckout(): List<List<OrderDto>> {
    val groups = LinkedHashMap<String, MutableList<OrderDto>>()
    forEach { o -> groups.getOrPut(o.checkoutId?.let { "c:$it" } ?: "o:${o.orderId}") { mutableListOf() } += o }
    return groups.values.toList()
}

/** Status counts, e.g. "2 approved, 1 cancelled". */
fun List<OrderDto>.statusSummary(): String =
    groupBy { it.status }.entries.joinToString(", ") { (s, list) -> "${list.size} ${s.label().lowercase()}" }

fun List<OrderDto>.hasBilled(): Boolean = any { it.status == OrderStatus.SOLD || it.status == OrderStatus.RETURNED }

/** Expandable header row of a multi-item checkout (no scale, row shade only). */
@Composable
fun CheckoutGroupRow(orders: List<OrderDto>, expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        leadingContent = { ProductImage(orders.first().imageUrl, null, size = 56.dp) },
        headlineContent = {
            Text("Checkout · ${orders.size} items", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    orders.joinToString { it.productName.capitalizeEachWord() },
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(orders.first().createdAt.dateTimeLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                Text(orders.statusSummary(), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(orders.sumOf { it.totalAmount }.inr(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse checkout" else "Expand checkout"
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Transparent),
        modifier = modifier.fillMaxWidth().shadeRow(onToggle)
    )
}
