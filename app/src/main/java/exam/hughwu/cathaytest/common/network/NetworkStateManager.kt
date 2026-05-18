package exam.hughwu.cathaytest.common.network

import android.content.Context
import android.net.ConnectivityManager
import exam.hughwu.repository.NetworkStateRepository
import kotlinx.coroutines.flow.Flow

object NetworkStateManager {
    private lateinit var networkStateRepository: NetworkStateRepository

    lateinit var networkStateFlow: Flow<Boolean> private set

    fun init(context: Context) {
        networkStateRepository = NetworkStateRepository(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
        networkStateFlow = networkStateRepository.observeNetworkState()
    }

    fun isNetworkConnected(): Boolean {
        return networkStateRepository.isNetworkConnected()
    }

}