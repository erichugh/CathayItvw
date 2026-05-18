package exam.hughwu.cathaytest.feature.stocklist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.LaunchedEffect
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.feature.stocklist.StockListEvent
import exam.hughwu.cathaytest.feature.stocklist.StockListIntent
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState.SortOrder
import exam.hughwu.cathaytest.feature.stocklist.StockListViewModel
import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil
import exam.hughwu.cathaytest.usecase.vo.StockVo

/**
 * Pure-Compose stock list: LazyColumn of StockCard, a Compose
 * ModalBottomSheet sort picker, and a Compose AlertDialog detail dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(viewModel: StockListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    var sortSheetFor by remember { mutableStateOf<SortOrder?>(null) }
    var detailStock by remember { mutableStateOf<StockVo?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is StockListEvent.ShowSortSheet -> sortSheetFor = event.current
                    is StockListEvent.ShowDetailDialog -> detailStock = event.stock
                    is StockListEvent.ShowError ->
                        snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        // Inset the whole screen (app bar + body) off the side navigation bar /
        // camera cutout and the bottom nav, matching the XML variants' root
        // padding — so the green toolbar never shows under the system controls.
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.systemBars
                .union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        ),
        topBar = {
            // Mirror the XML variants' AppBar: a black scrim fills the status-
            // bar (top) inset so the green toolbar color never shows behind the
            // control bar; the green TopAppBar sits below it.
            Column(Modifier.background(Color.Black)) {
                Spacer(
                    Modifier.windowInsetsTopHeight(
                        WindowInsets.systemBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Top),
                    ),
                )
                TopAppBar(
                    title = { Text(stringResource(R.string.title_variant_compose)) },
                    actions = {
                        IconButton(onClick = {
                            viewModel.sendIntent(StockListIntent.OnSortIconClicked)
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_filter),
                                contentDescription = stringResource(R.string.action_filter),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    // Top inset is the black Spacer above; horizontal inset is
                    // applied to the whole Scaffold — so the bar adds none.
                    windowInsets = WindowInsets(0, 0, 0, 0),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        // Insets are handled explicitly below (mirrors the XML variants: the
        // app bar owns the top inset, the body owns the sides + bottom), so
        // Scaffold itself adds none and `innerPadding` is just the bar height.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val listState = rememberLazyListState()
        // Snap to the top of the new ordering whenever the sort order changes
        // (mirrors the XML / Hybrid variants' scrollToPosition(0)). Also fires
        // once on first composition, which is a harmless no-op on an empty list.
        LaunchedEffect(uiState.sortOrder) {
            listState.scrollToItem(0)
        }
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.sendIntent(StockListIntent.Refresh) },
            // Side / bottom insets are applied to the whole Scaffold; only the
            // app-bar height needs handling here.
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                items(uiState.stocks, key = { it.code }) { stock ->
                    StockCard(
                        stock = stock,
                        onClick = {
                            viewModel.sendIntent(
                                StockListIntent.OnItemClicked(stock.code),
                            )
                        },
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (sortSheetFor != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { sortSheetFor = null },
            sheetState = sheetState,
        ) {
            SortOption(
                label = stringResource(R.string.sort_by_code_desc),
                selected = sortSheetFor == SortOrder.CodeDesc,
                onClick = {
                    viewModel.sendIntent(
                        StockListIntent.OnSortSelected(SortOrder.CodeDesc),
                    )
                    sortSheetFor = null
                },
            )
            SortOption(
                label = stringResource(R.string.sort_by_code_asc),
                selected = sortSheetFor == SortOrder.CodeAsc,
                onClick = {
                    viewModel.sendIntent(
                        StockListIntent.OnSortSelected(SortOrder.CodeAsc),
                    )
                    sortSheetFor = null
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    detailStock?.let { stock ->
        StockDetailAlertDialog(
            stock = stock,
            onDismiss = { detailStock = null },
        )
    }
}

@Composable
private fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun StockDetailAlertDialog(
    stock: StockVo,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val title = DisplayStockUtil.formatStockDetailTitle(
        context = context,
        stockCode = stock.code,
        stockName = stock.name
    )

    val body = DisplayStockUtil.formatStockDetailBody(
        context = context,
        peRatio = stock.peRatio,
        dividendYield = stock.dividendYield,
        pbRatio = stock.pbRatio,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_dismiss))
            }
        },
    )
}
