package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.data.v1.dto.ProductCategoryDto
import com.saurabh.mediuserapp.repo.ProductFilter
import com.saurabh.mediuserapp.utils.capitalizeEachWord

private val SORTS = listOf("name" to "Name", "price" to "Price", "mrp" to "MRP", "updated_at" to "Recently updated")
private val SCHEDULES = listOf("OTC", "H", "H1", "X", "NARCOTIC")

/** Filters for GET products: category, schedule_type, requires_prescription, in_stock, sort, order. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductFilterSheet(
    filter: ProductFilter,
    sort: String,
    order: String,
    categories: List<ProductCategoryDto>,
    onApply: (ProductFilter, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var category by rememberSaveable { mutableStateOf(filter.category) }
    var schedule by rememberSaveable { mutableStateOf(filter.scheduleType) }
    var rx by rememberSaveable { mutableStateOf(filter.requiresPrescription) }
    var inStock by rememberSaveable { mutableStateOf(filter.inStock == true) }
    var sortKey by rememberSaveable { mutableStateOf(sort) }
    var sortOrder by rememberSaveable { mutableStateOf(order) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            "Filter & sort", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() }
        )
        Column(
            Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            SheetLabel("Sort by")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SORTS.forEach { (key, label) ->
                    GsFilterChip(selected = sortKey == key, onClick = { sortKey = key }, label = { ChipLabel(label) })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GsFilterChip(selected = sortOrder == "asc", onClick = { sortOrder = "asc" }, label = { ChipLabel("Ascending") })
                GsFilterChip(selected = sortOrder == "desc", onClick = { sortOrder = "desc" }, label = { ChipLabel("Descending") })
            }

            SheetLabel("Availability")
            GsFilterChip(selected = inStock, onClick = { inStock = !inStock }, label = { ChipLabel("In stock only") })

            SheetLabel("Prescription")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GsFilterChip(selected = rx == null, onClick = { rx = null }, label = { ChipLabel("Any") })
                GsFilterChip(selected = rx == true, onClick = { rx = true }, label = { ChipLabel("Rx required") })
                GsFilterChip(selected = rx == false, onClick = { rx = false }, label = { ChipLabel("No Rx") })
            }

            SheetLabel("Schedule")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SCHEDULES.forEach { s ->
                    GsFilterChip(
                        selected = schedule == s,
                        onClick = { schedule = if (schedule == s) null else s },
                        label = { ChipLabel(if (s == "OTC") s else "Schedule $s") }
                    )
                }
            }

            if (categories.isNotEmpty()) {
                SheetLabel("Category")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    categories.forEach { c ->
                        val same = category.equals(c.name, ignoreCase = true)
                        GsFilterChip(
                            selected = same,
                            onClick = { category = if (same) null else c.name },
                            label = { ChipLabel("${c.name.capitalizeEachWord()} (${c.productCount})") }
                        )
                    }
                }
            }

        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                ShadeOutlinedButton(
                    text = "Reset",
                    onClick = {
                        category = null; schedule = null; rx = null; inStock = false
                        sortKey = "name"; sortOrder = "asc"
                    },
                    modifier = Modifier.weight(1f)
                )
                ShadeButton(
                    text = "Apply",
                    onClick = {
                        onApply(
                            filter.copy(category = category, scheduleType = schedule, requiresPrescription = rx, inStock = if (inStock) true else null),
                            sortKey, sortOrder
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp)
    )
}
