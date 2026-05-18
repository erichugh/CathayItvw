package exam.hughwu.cathaytest.feature.stocklist.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.common.BaseFragment
import exam.hughwu.cathaytest.databinding.FragmentStockListXmlBinding
import exam.hughwu.cathaytest.extension.applyHorizontalAndBottomInsets
import exam.hughwu.cathaytest.feature.stocklist.StockListEvent
import exam.hughwu.cathaytest.feature.stocklist.StockListIntent
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState
import exam.hughwu.cathaytest.feature.stocklist.StockListViewModel
import exam.hughwu.cathaytest.feature.stocklist.adapter.XmlStockAdapter
import exam.hughwu.cathaytest.feature.stocklist.dialog.StockDetailDialog
import exam.hughwu.cathaytest.feature.stocklist.dialog.StockSortBottomSheet

/**
 * Pure-XML variant of the stock list (item rows are `item_stock_card.xml`
 * via [exam.hughwu.cathaytest.feature.stocklist.adapter.XmlStockAdapter], no Compose at all).
 *
 * Behaviour is identical to [exam.hughwu.cathaytest.feature.stocklist.hybrid.StockListFragment]; the only differences are the
 * adapter (pure XML), the layout, and that the [exam.hughwu.cathaytest.feature.stocklist.StockListViewModel] is
 * activity-scoped ([activityViewModels]) so all three variants share one
 * instance and a single sort state.
 */
@AndroidEntryPoint
class StockListXmlFragment :
    BaseFragment<FragmentStockListXmlBinding, StockListUiState, StockListIntent, StockListEvent, StockListViewModel, >() {

    override val viewModel: StockListViewModel by activityViewModels()

    private val stockAdapter by lazy {
        XmlStockAdapter(onItemClick = { stock ->
            viewModel.sendIntent(StockListIntent.OnItemClicked(stock.code))
        })
    }

    /** Tracks the previously rendered sort order to detect changes that warrant scrolling to top. */
    private var lastRenderedSortOrder: StockListUiState.SortOrder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStockListXmlBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return
        with(b) {
            root.applyHorizontalAndBottomInsets()

            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = stockAdapter
                setHasFixedSize(true)
                // Snap-update (no item animator) keeps re-sorts of 1000+ rows
                // instant; the initial fade-in is still driven by `layoutAnimation`.
                itemAnimator = null
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.sendIntent(StockListIntent.Refresh)
            }

            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_filter) {
                    viewModel.sendIntent(StockListIntent.OnSortIconClicked)
                    true
                } else false
            }
        }

        // Receive selection from StockSortBottomSheet
        parentFragmentManager.setFragmentResultListener(
            StockSortBottomSheet.Companion.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val orderName = bundle.getString(StockSortBottomSheet.Companion.RESULT_SORT_ORDER)
                ?: return@setFragmentResultListener
            val order = runCatching {
                StockListUiState.SortOrder.valueOf(orderName)
            }.getOrNull() ?: return@setFragmentResultListener
            viewModel.sendIntent(StockListIntent.OnSortSelected(order))
        }

        // Shared (activity-scoped) ViewModel: load only if no data yet, so
        // switching between variants reuses the already-loaded state.
        if (viewModel.uiState.value.stocks.isEmpty()) {
            viewModel.sendIntent(StockListIntent.Init)
        }
    }

    override fun onUiStateChanged(uiState: StockListUiState) {
        val b = binding ?: return
        with(b) {
            progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
            swipeRefresh.isRefreshing = uiState.isRefreshing

            val firstFill = stockAdapter.itemCount == 0 && uiState.stocks.isNotEmpty()
            val sortChanged = lastRenderedSortOrder != null &&
                lastRenderedSortOrder != uiState.sortOrder
            lastRenderedSortOrder = uiState.sortOrder

            if (sortChanged) {
                stockAdapter.submitList(null) {
                    stockAdapter.submitList(uiState.stocks) {
                        recyclerView.scrollToPosition(0)
                    }
                }
            } else {
                stockAdapter.submitList(uiState.stocks) {
                    if (firstFill) recyclerView.scheduleLayoutAnimation()
                }
            }
        }
    }

    override fun onUiEvent(uiEvent: StockListEvent) {
        when (uiEvent) {
            is StockListEvent.ShowSortSheet -> {
                StockSortBottomSheet.newInstance(uiEvent.current)
                    .show(parentFragmentManager, "StockSortBottomSheet")
            }
            is StockListEvent.ShowDetailDialog -> {
                StockDetailDialog.show(requireContext(), uiEvent.stock)
            }
            is StockListEvent.ShowError -> {
                Toast.makeText(requireContext(), uiEvent.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}