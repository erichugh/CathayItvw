package exam.hughwu.cathaytest.feature.home

import dagger.hilt.android.lifecycle.HiltViewModel
import exam.hughwu.cathaytest.common.BaseViewModel
import exam.hughwu.cathaytest.common.NoEvent
import exam.hughwu.cathaytest.common.NoUiState
import exam.hughwu.cathaytest.common.NoIntent
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() :
    BaseViewModel<NoUiState, NoIntent, NoEvent>(NoUiState)  {
    override suspend fun handleIntent(intent: NoIntent) = Unit
}