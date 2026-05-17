package exam.hughwu.cathaytest.feature.stocklist

import exam.hughwu.cathaytest.common.UiState
import exam.hughwu.cathaytest.usecase.vo.StockVo

data class StockListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val stocks: List<StockVo> = emptyList(),
    val sortOrder: SortOrder = SortOrder.CodeDesc,
    val errorMessage: String? = null,
) : UiState {

    enum class SortOrder { CodeAsc, CodeDesc }

    companion object {
        fun initial() = StockListUiState()
    }
}
