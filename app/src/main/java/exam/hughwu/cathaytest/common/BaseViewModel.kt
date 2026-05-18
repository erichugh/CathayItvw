package exam.hughwu.cathaytest.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import exam.hughwu.cathaytest.common.network.NetworkStateProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Generic MVI base ViewModel
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEvent>(
    initialState: S,
    protected val networkStateProvider: NetworkStateProvider,
) : ViewModel() {

    /** Live connectivity stream, for screens that re-render on changes. */
    val networkState: Flow<Boolean> get() = networkStateProvider.networkStateFlow

    protected val mutableUiState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = mutableUiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<E> = _uiEvent.asSharedFlow()

    private val intentChannel = Channel<I>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (intent in intentChannel) {
                handleIntent(intent)
            }
        }
    }

    fun sendIntent(intent: I) {
        intentChannel.trySend(intent)
    }

    protected suspend fun emitUiEvent(event: E) {
        _uiEvent.emit(event)
    }

    abstract suspend fun handleIntent(intent: I)

    fun startActionWithNoNetworkHandling(
        networkAction: () -> Unit,
        showNoNetworkMessage: () -> Unit = {},
    ) {
        if (networkStateProvider.isNetworkConnected()) {
            networkAction()
        } else {
            handleNoNetworkCondition()
            showNoNetworkMessage()
        }
    }

    open fun handleNoNetworkCondition() {
        // override this function to handle no network condition
    }
}
