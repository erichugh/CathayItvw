package exam.hughwu.cathaytest.feature.stocklist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exam.hughwu.cathaytest.common.BaseViewModel
import exam.hughwu.cathaytest.common.network.NetworkStateProvider
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState.SortOrder
import exam.hughwu.cathaytest.usecase.GetMergedStocksUseCase
import exam.hughwu.cathaytest.usecase.vo.StockVo
import exam.hughwu.repository.preferences.SortPreferenceRepository
import exam.hughwu.repository.preferences.StoredSortOrder
import exam.hughwu.retrofit.UseCaseResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockListViewModel @Inject constructor(
    private val getMergedStocks: GetMergedStocksUseCase,
    private val sortPreference: SortPreferenceRepository,
    networkStateProvider: NetworkStateProvider,
) : BaseViewModel<StockListUiState, StockListIntent, StockListEvent>(
    StockListUiState.initial(),
    networkStateProvider,
) {

    init {
        // Reactively reflect any sort-preference change (incl. first read) into UI state.
        viewModelScope.launch {
            sortPreference.sortOrderFlow.collect { stored ->
                val order = stored.toUi()
                mutableUiState.update { state ->
                    state.copy(
                        sortOrder = order,
                        stocks = sortStocks(state.stocks, order),
                    )
                }
            }
        }
    }

    override suspend fun handleIntent(intent: StockListIntent) {
        when (intent) {
            StockListIntent.Init -> {
                // Wait for the first DataStore emission so loadStocks sorts with
                // the persisted preference, not the data class default.
                sortPreference.sortOrderFlow.first()
                loadStocks(initial = true)
            }
            StockListIntent.Refresh -> loadStocks(initial = false)
            StockListIntent.OnSortIconClicked -> {
                emitUiEvent(StockListEvent.ShowSortSheet(mutableUiState.value.sortOrder))
            }
            is StockListIntent.OnSortSelected -> {
                // Skip if user picked the same order — keep scroll position untouched.
                if (intent.order == mutableUiState.value.sortOrder) return
                // Persist; the init { } collector above will update UI state.
                // Fragment scrolls to top in onUiStateChanged when sortOrder differs.
                sortPreference.setSortOrder(intent.order.toStored())
            }
            is StockListIntent.OnItemClicked -> {
                mutableUiState.value.stocks
                    .firstOrNull { it.code == intent.code }
                    ?.let { emitUiEvent(StockListEvent.ShowDetailDialog(it)) }
            }
        }
    }

    private suspend fun loadStocks(initial: Boolean) {
        mutableUiState.update {
            it.copy(
                isLoading = initial && it.stocks.isEmpty(),
                isRefreshing = !initial,
                errorMessage = null,
            )
        }
        // UseCase is a Flow: it emits a refined merged list each time one of the three
        // TWSE endpoints returns. Push every emission straight to UiState so the user
        // sees partial data as soon as the fastest endpoint replies.
        getMergedStocks().collect { result ->
            when (result) {
                is UseCaseResponse.Success -> {
                    val sorted = sortStocks(result.data.orEmpty(), mutableUiState.value.sortOrder)
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            stocks = sorted,
                            errorMessage = null,
                        )
                    }
                }
                is UseCaseResponse.Failure -> {
                    // Only emitted when all three endpoints failed (UseCase contract).
                    val msg = result.errorBody?.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = msg,
                        )
                    }
                    emitUiEvent(StockListEvent.ShowError(msg))
                }
            }
        }
    }

    companion object {
        internal fun sortStocks(list: List<StockVo>, order: SortOrder): List<StockVo> = when (order) {
            SortOrder.CodeAsc -> list.sortedBy { it.code }
            SortOrder.CodeDesc -> list.sortedByDescending { it.code }
        }

        internal fun StoredSortOrder.toUi(): SortOrder = when (this) {
            StoredSortOrder.CODE_ASC -> SortOrder.CodeAsc
            StoredSortOrder.CODE_DESC -> SortOrder.CodeDesc
        }

        internal fun SortOrder.toStored(): StoredSortOrder = when (this) {
            SortOrder.CodeAsc -> StoredSortOrder.CODE_ASC
            SortOrder.CodeDesc -> StoredSortOrder.CODE_DESC
        }
    }
}
