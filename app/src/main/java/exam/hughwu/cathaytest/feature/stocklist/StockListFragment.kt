package exam.hughwu.cathaytest.feature.stocklist

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
import exam.hughwu.cathaytest.common.applyHorizontalAndBottomInsets
import exam.hughwu.cathaytest.databinding.FragmentStockListBinding
import exam.hughwu.cathaytest.feature.stocklist.adapter.StockAdapter
import exam.hughwu.cathaytest.feature.stocklist.dialog.StockDetailDialog
import exam.hughwu.cathaytest.feature.stocklist.dialog.StockSortBottomSheet

@AndroidEntryPoint
class StockListFragment :
    BaseFragment<FragmentStockListBinding, StockListUiState, StockListIntent, StockListEvent, StockListViewModel>() {

    override val viewModel: StockListViewModel by activityViewModels()

    private val stockAdapter by lazy {
        StockAdapter(onItemClick = { stock ->
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
        binding = FragmentStockListBinding.inflate(inflater, container, false)
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
            StockSortBottomSheet.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val orderName = bundle.getString(StockSortBottomSheet.RESULT_SORT_ORDER) ?: return@setFragmentResultListener
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
                // Re-sorting 1000+ items via ListAdapter's default move-diff is O(N²) on
                // a complete reorder. Bypass move computation by clearing the list first
                // (N removes, O(N)) and then re-submitting (N inserts against empty,
                // O(N)). Net: snappy snap to top of new order.
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
