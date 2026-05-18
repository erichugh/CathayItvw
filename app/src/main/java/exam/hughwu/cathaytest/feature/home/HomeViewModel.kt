package exam.hughwu.cathaytest.feature.home

import dagger.hilt.android.lifecycle.HiltViewModel
import exam.hughwu.cathaytest.common.BaseViewModel
import exam.hughwu.cathaytest.common.NoEvent
import exam.hughwu.cathaytest.common.NoUiState
import exam.hughwu.cathaytest.common.NoIntent
import exam.hughwu.cathaytest.common.network.NetworkStateProvider
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    networkStateProvider: NetworkStateProvider,
) : BaseViewModel<NoUiState, NoIntent, NoEvent>(NoUiState, networkStateProvider) {
    override suspend fun handleIntent(intent: NoIntent) = Unit
}