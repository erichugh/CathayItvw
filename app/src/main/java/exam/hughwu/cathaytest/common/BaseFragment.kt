package exam.hughwu.cathaytest.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import exam.hughwu.cathaytest.extension.collectIn
import kotlinx.coroutines.cancelChildren

/**
 * Generic MVI base Fragment.
 */
abstract class BaseFragment<
    VB : ViewBinding, S : UiState, I : UiIntent, E : UiEvent, VM : BaseViewModel<S, I, E>>
    : Fragment() {

    protected var binding: VB? = null

    protected abstract val viewModel: VM

    abstract fun onUiStateChanged(uiState: S)

    abstract fun onUiEvent(uiEvent: E)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryToObserveUiState()
        tryToObserveUiEvent()
    }

    open fun tryToObserveUiState() {
        viewModel.uiState.collectIn(viewLifecycleOwner) { newUiState ->
            onUiStateChanged(newUiState)
        }
    }

    open fun tryToObserveUiEvent() {
        viewModel.uiEvent.collectIn(viewLifecycleOwner) {
            if (it != NoEvent) {
                onUiEvent(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
        binding = null
    }
}
