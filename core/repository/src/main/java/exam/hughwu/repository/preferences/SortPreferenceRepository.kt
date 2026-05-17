package exam.hughwu.repository.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists user-selected sort preference for the stock list across app restarts.
 */
enum class StoredSortOrder { CODE_ASC, CODE_DESC }

private val Context.stockSortDataStore by preferencesDataStore(name = "stock_prefs")

@Singleton
class SortPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = stringPreferencesKey(KEY_SORT_ORDER)

    val sortOrderFlow: Flow<StoredSortOrder> = context.stockSortDataStore.data.map { prefs ->
        prefs[key]
            ?.let { runCatching { StoredSortOrder.valueOf(it) }.getOrNull() }
            ?: StoredSortOrder.CODE_DESC
    }

    suspend fun setSortOrder(order: StoredSortOrder) {
        context.stockSortDataStore.edit { prefs -> prefs[key] = order.name }
    }

    private companion object {
        const val KEY_SORT_ORDER = "sort_order"
    }
}
