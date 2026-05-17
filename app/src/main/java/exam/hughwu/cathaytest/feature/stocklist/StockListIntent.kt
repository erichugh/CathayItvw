package exam.hughwu.cathaytest.feature.stocklist

import exam.hughwu.cathaytest.common.UiIntent

sealed class StockListIntent : UiIntent {
    object Init : StockListIntent()
    object Refresh : StockListIntent()
    object OnSortIconClicked : StockListIntent()
    data class OnSortSelected(val order: StockListUiState.SortOrder) : StockListIntent()
    data class OnItemClicked(val code: String) : StockListIntent()
}
