package exam.hughwu.cathaytest.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

/**
 * Generic MVI base Fragment.
 */
abstract class BaseFragment<
    VB : ViewBinding, S : UiState, I : UiIntent, E : UiEvent, VM : BaseViewModel<S, I, E>, >
    : Fragment() {

    protected var binding: VB? = null

    protected abstract val viewModel: VM

    abstract fun onUiStateChanged(uiState: S)

    abstract fun onUiEvent(uiEvent: E)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state -> onUiStateChanged(state) }
                }
                launch {
                    viewModel.uiEvent.collect { event -> onUiEvent(event) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
