package exam.hughwu.cathaytest.feature.stocklist.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.common.BaseFragment
import exam.hughwu.cathaytest.feature.stocklist.StockListEvent
import exam.hughwu.cathaytest.feature.stocklist.StockListIntent
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState
import exam.hughwu.cathaytest.feature.stocklist.StockListViewModel
import exam.hughwu.cathaytest.feature.stocklist.composable.AppTheme
import exam.hughwu.cathaytest.feature.stocklist.composable.StockListScreen

/**
 * Pure-Compose variant: a single ComposeView rendering StockListScreen
 * (LazyColumn + Compose ModalBottomSheet + Compose AlertDialog).
 *
 */
@AndroidEntryPoint
class StockListComposeFragment :
    BaseFragment<ViewBinding, StockListUiState, StockListIntent, StockListEvent, StockListViewModel>() {

    override val viewModel: StockListViewModel by hiltNavGraphViewModels(R.id.stockGraph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                AppTheme {
                    StockListScreen(viewModel = viewModel)
                }
            }
        }
        binding = ViewBinding { composeView }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Shared (activity-scoped) ViewModel: load only if no data yet, so
        // switching between variants reuses the already-loaded state.
        if (viewModel.uiState.value.stocks.isEmpty()) {
            viewModel.sendIntent(StockListIntent.Init)
        }
    }

    // StockListScreen collects uiState via collectAsStateWithLifecycle.
    override fun onUiStateChanged(uiState: StockListUiState) = Unit

    // StockListScreen collects uiEvent via LaunchedEffect / repeatOnLifecycle.
    override fun onUiEvent(uiEvent: StockListEvent) = Unit
}