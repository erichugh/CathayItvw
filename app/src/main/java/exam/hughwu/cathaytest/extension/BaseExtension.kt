package exam.hughwu.cathaytest.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job {
    check(minActiveState === Lifecycle.State.STARTED || minActiveState === Lifecycle.State.RESUMED) {
        "minActiveState must be STARTED or RESUMED"
    }
    return owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(state = minActiveState) { collect { action(it) } }
    }
}