package com.saurabh.mediuserapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.saurabh.mediuserapp.ui.components.DrawerAvatarButton
import com.saurabh.mediuserapp.ui.components.pressScale
import com.saurabh.mediuserapp.ui.components.shadeColor
import com.saurabh.mediuserapp.ui.theme.GsUi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediuserapp.data.v1.core.UiState
import com.saurabh.mediuserapp.data.v1.dto.ProductDto
import com.saurabh.mediuserapp.repo.ProductFilter
import com.saurabh.mediuserapp.ui.adaptive.adaptiveGridCells
import com.saurabh.mediuserapp.ui.components.AppScreenScaffold
import com.saurabh.mediuserapp.ui.components.ChipLabel
import com.saurabh.mediuserapp.ui.components.EmptyState
import com.saurabh.mediuserapp.ui.components.ErrorState
import com.saurabh.mediuserapp.ui.components.GsFilterChip
import com.saurabh.mediuserapp.ui.components.GsTextField
import com.saurabh.mediuserapp.ui.components.ProductFilterSheet
import com.saurabh.mediuserapp.ui.components.ProductImage
import com.saurabh.mediuserapp.ui.components.ProductStockChip
import com.saurabh.mediuserapp.ui.components.SkeletonList
import com.saurabh.mediuserapp.ui.components.TileIconButton
import com.saurabh.mediuserapp.ui.components.Tone
import com.saurabh.mediuserapp.ui.components.ToneChip
import com.saurabh.mediuserapp.ui.components.inr
import com.saurabh.mediuserapp.ui.components.needsPrescription
import com.saurabh.mediuserapp.ui.components.shadeRow
import com.saurabh.mediuserapp.ui.nav.Routes
import com.saurabh.mediuserapp.ui.theme.Transparent
import com.saurabh.mediuserapp.utils.capitalizeEachWord
import com.saurabh.mediuserapp.viewModel.MyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: MyViewModel, navController: NavController, onOpenDrawer: () -> Unit = {}) {
    val state by viewModel.getAllProduct.collectAsStateWithLifecycle()
    val filter by viewModel.productFilter.collectAsStateWithLifecycle()
    val sort by viewModel.productSort.collectAsStateWithLifecycle()
    val categories by viewModel.categoriesState.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val userState by viewModel.specificUserState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { if (userState.success == null && !userState.isLoading) viewModel.getSpecificUser() }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.getAllProduct(); viewModel.loadProductCategories() }

    var query by rememberSaveable { mutableStateOf(filter.q.orEmpty()) }
    LaunchedEffect(query) {
        val q = query.trim().ifBlank { null }
        if (q == viewModel.productFilter.value.q) return@LaunchedEffect
        delay(400)
        viewModel.setProductFilter(viewModel.productFilter.value.copy(q = q))
    }

    val gridState = rememberLazyGridState()
    val loadMore by rememberUpdatedState { viewModel.loadMoreProducts() }
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }.distinctUntilChanged().collect { if (it) loadMore() }
    }
    val categoryList = (categories as? UiState.Success)?.data.orEmpty()
    val activeFilters = listOfNotNull(filter.category, filter.scheduleType, filter.requiresPrescription, filter.inStock).size

    AppScreenScaffold(
        title = "Medicines",
        navigationIcon = {
            DrawerAvatarButton(
                url = userState.success?.imageUrl,
                name = userState.success?.name.orEmpty(),
                onClick = { focus.clearFocus(); onOpenDrawer() },
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        actions = {
            TileIconButton(
                Icons.Outlined.QrCodeScanner, "Scan barcode",
                { navController.navigate(Routes.BarcodeScannerRoutes) { launchSingleTop = true } },
                Modifier.padding(end = 8.dp)
            )
        }
    ) { inner ->
        val items = state.success?.items
        PullToRefreshBox(
            isRefreshing = state.isLoading && items != null,
            onRefresh = { viewModel.getAllProduct(force = true); viewModel.loadProductCategories(force = true) },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = adaptiveGridCells(320.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "search", span = { GridItemSpan(maxLineSpan) }, contentType = "search") {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GsTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search medicine, generic, brand", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotEmpty()) {
                            { IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") } }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    )
                    FilterSortButton(activeFilters) { focus.clearFocus(); showFilters = true }
                  }
                }
                item(key = "chips", span = { GridItemSpan(maxLineSpan) }, contentType = "chips") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "instock") {
                            GsFilterChip(
                                selected = filter.inStock == true,
                                onClick = { viewModel.setProductFilter(filter.copy(inStock = if (filter.inStock == true) null else true)) },
                                label = { ChipLabel("In stock") }
                            )
                        }
                        item(key = "rx") {
                            GsFilterChip(
                                selected = filter.requiresPrescription == false,
                                onClick = {
                                    viewModel.setProductFilter(filter.copy(requiresPrescription = if (filter.requiresPrescription == false) null else false))
                                },
                                label = { ChipLabel("No prescription") }
                            )
                        }
                        items(categoryList, key = { "c${it.name}" }) { c ->
                            val same = filter.category.equals(c.name, ignoreCase = true)
                            GsFilterChip(
                                selected = same,
                                onClick = { viewModel.setProductFilter(filter.copy(category = if (same) null else c.name)) },
                                label = { ChipLabel(c.name.capitalizeEachWord()) }
                            )
                        }
                    }
                }
                when {
                    items == null && state.error != null -> item(key = "error", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllProduct(force = true) })
                    }
                    items == null -> item(key = "skeleton", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        SkeletonList(rows = 6, modifier = Modifier.padding(0.dp))
                    }
                    items.isEmpty() -> item(key = "empty", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        val filtered = filter != ProductFilter()
                        EmptyState(
                            title = if (filtered) "No matching medicines" else "No medicines yet",
                            message = if (filtered) "Try a different search or filter." else "Please check back later."
                        )
                    }
                    else -> {
                        items(items, key = { it.productId }, contentType = { "product" }) { p ->
                            ProductRow(p) {
                                navController.navigate(Routes.SpecificProductRoutes(p.productId)) { launchSingleTop = true }
                            }
                        }
                        if (state.isLoadingMore) item(key = "more", span = { GridItemSpan(maxLineSpan) }, contentType = "footer") {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        ProductFilterSheet(
            filter = filter,
            sort = sort.sort ?: "name",
            order = sort.order ?: "asc",
            categories = categoryList,
            onApply = { f, s, o -> showFilters = false; viewModel.setProductFilter(f, s, o) },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun FilterSortButton(activeFilters: Int, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val bg by source.shadeColor(GsUi.rowShade(), "filterBtn")
    val scale by source.pressScale()
    val shape = MaterialTheme.shapes.medium
    Box(
        Modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bg)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (activeFilters > 0) "Filter and sort ($activeFilters active)" else "Filter and sort" },
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(badge = {
            if (activeFilters > 0) Badge(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Text(activeFilters.toString(), maxLines = 1)
            }
        }) {
            Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ProductRow(p: ProductDto, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().shadeRow(onClick)) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Transparent),
            leadingContent = { ProductImage(p.imageUrl, p.dosageForm, size = 64.dp) },
            headlineContent = {
                Text(p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        listOf(p.strength, p.packSize).filter { it.isNotBlank() }.joinToString(" · ")
                            .ifBlank { p.category.capitalizeEachWord() },
                        style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProductStockChip(p)
                        if (p.needsPrescription()) ToneChip("Rx", Tone.Info)
                    }
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(p.price.inr(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    if (p.mrp > p.price) Text(
                        p.mrp.inr(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1
                    )
                }
            }
        )
    }
}
