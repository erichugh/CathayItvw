package exam.hughwu.cathaytest.feature.stocklist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.databinding.ItemStockCardBinding
import exam.hughwu.cathaytest.feature.stocklist.vo.DisplayStockUtil
import exam.hughwu.cathaytest.feature.stocklist.vo.DisplayStockUtil.PriceTrend
import exam.hughwu.cathaytest.usecase.vo.StockVo

/**
 * Pure-XML counterpart of StockAdapter: each row is the prepared
 * `item_stock_card.xml`
 */
class XmlStockAdapter(
    private val onItemClick: (StockVo) -> Unit,
) : ListAdapter<StockVo, XmlStockAdapter.XmlStockViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XmlStockViewHolder {
        val binding = ItemStockCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return XmlStockViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: XmlStockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class XmlStockViewHolder(
        private val binding: ItemStockCardBinding,
        private val onItemClick: (StockVo) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stock: StockVo) = with(binding) {
            val ctx = root.context
            @ColorInt val defaultColor =
                ContextCompat.getColor(ctx, R.color.scheme_on_surface)

            tvCode.text = DisplayStockUtil.textOrPlaceholder(stock.code)
            tvName.text = DisplayStockUtil.textOrPlaceholder(stock.name)

            tvOpeningPrice.text = DisplayStockUtil.formatNumber(stock.openingPrice)
            tvClosingPrice.text = DisplayStockUtil.formatNumber(stock.closingPrice)
            tvHighestPrice.text = DisplayStockUtil.formatNumber(stock.highestPrice)
            tvLowestPrice.text = DisplayStockUtil.formatNumber(stock.lowestPrice)
            tvChange.text = DisplayStockUtil.formatSignedNumber(stock.change)
            tvMonthlyAvg.text = DisplayStockUtil.formatNumber(stock.monthlyAveragePrice)
            tvTransaction.text = DisplayStockUtil.formatNumber(stock.transaction)
            tvTradeVolume.text = DisplayStockUtil.formatNumber(stock.tradeVolume)
            tvTradeValue.text = DisplayStockUtil.formatNumber(stock.tradeValue)

            // Recycled views must be reset every bind, then only the two
            // trend-driven fields get an accent color (mirrors StockCard).
            tvOpeningPrice.setTextColor(defaultColor)
            tvHighestPrice.setTextColor(defaultColor)
            tvLowestPrice.setTextColor(defaultColor)
            tvMonthlyAvg.setTextColor(defaultColor)
            tvTransaction.setTextColor(defaultColor)
            tvTradeVolume.setTextColor(defaultColor)
            tvTradeValue.setTextColor(defaultColor)

            tvClosingPrice.setTextColor(
                trendColorInt(
                    ctx,
                    DisplayStockUtil.closingVsAverage(
                        stock.closingPrice, stock.monthlyAveragePrice,
                    ),
                ),
            )
            tvChange.setTextColor(
                trendColorInt(ctx, DisplayStockUtil.changeTrend(stock.change)),
            )

            cardStock.setOnClickListener { onItemClick(stock) }
        }

        @ColorInt
        private fun trendColorInt(
            ctx: android.content.Context,
            trend: PriceTrend,
        ): Int = ContextCompat.getColor(
            ctx,
            when (trend) {
                PriceTrend.UP -> R.color.trend_up
                PriceTrend.DOWN -> R.color.trend_down
                PriceTrend.FLAT -> R.color.trend_flat
                PriceTrend.UNKNOWN -> R.color.scheme_on_surface
            },
        )
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
