package exam.hughwu.cathaytest.feature.stocklist.adapter

import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import exam.hughwu.cathaytest.feature.stocklist.composable.AppTheme
import exam.hughwu.cathaytest.feature.stocklist.composable.StockCard
import exam.hughwu.cathaytest.usecase.vo.StockVo

/**
 * RecyclerView adapter that hosts a Compose StockCard inside each row
 */
class StockAdapter(
    private val onItemClick: (StockVo) -> Unit,
) : ListAdapter<StockVo, StockAdapter.StockViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val composeView = ComposeView(parent.context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }
        return StockViewHolder(composeView, onItemClick)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockViewHolder(
        private val composeView: ComposeView,
        private val onItemClick: (StockVo) -> Unit,
    ) : RecyclerView.ViewHolder(composeView) {

        private var stock by mutableStateOf<StockVo?>(null)

        init {
            composeView.setContent {
                AppTheme {
                    stock?.let { current ->
                        StockCard(
                            stock = current,
                            onClick = { onItemClick(current) },
                        )
                    }
                }
            }
        }

        fun bind(item: StockVo) {
            stock = item
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StockVo>() {
            override fun areItemsTheSame(oldItem: StockVo, newItem: StockVo) =
                oldItem.code == newItem.code

            override fun areContentsTheSame(oldItem: StockVo, newItem: StockVo) =
                oldItem == newItem
        }
    }
}
