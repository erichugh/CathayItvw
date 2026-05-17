package exam.hughwu.cathaytest.feature.stocklist

import exam.hughwu.cathaytest.common.UiEvent
import exam.hughwu.cathaytest.usecase.vo.StockVo

sealed class StockListEvent : UiEvent {
    data class ShowSortSheet(val current: StockListUiState.SortOrder) : StockListEvent()
    data class ShowDetailDialog(val stock: StockVo) : StockListEvent()
    data class ShowError(val message: String) : StockListEvent()
}
