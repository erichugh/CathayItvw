package exam.hughwu.cathaytest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import exam.hughwu.cathaytest.common.network.NetworkStateManager

@HiltAndroidApp
class CathayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkStateManager.init(this)
    }
}
