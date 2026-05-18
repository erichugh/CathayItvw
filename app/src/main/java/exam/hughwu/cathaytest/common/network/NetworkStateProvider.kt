package exam.hughwu.cathaytest.common.network

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import exam.hughwu.repository.NetworkStateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable wrapper around [NetworkStateRepository]. Replaces the old
 * `NetworkStateManager` object singleton so connectivity can be faked with
 * `@BindValue` in instrumented tests (same pattern as the mocked repository),
 * instead of relying on the test device's real network and a manual `init()`
 * that `HiltTestApplication` never calls.
 *
 * Constructor-injected with Hilt's built-in `@ApplicationContext`, so no Hilt
 * module is needed and `@BindValue` cleanly replaces it.
 */
@Singleton
class NetworkStateProvider @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val repository = NetworkStateRepository(
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    )

    val networkStateFlow: Flow<Boolean> = repository.observeNetworkState()

    fun isNetworkConnected(): Boolean = repository.isNetworkConnected()
}
