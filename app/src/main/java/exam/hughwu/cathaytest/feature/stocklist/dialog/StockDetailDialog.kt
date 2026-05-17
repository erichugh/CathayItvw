package exam.hughwu.cathaytest.feature.stocklist.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.feature.stocklist.vo.DisplayStockUtil
import exam.hughwu.cathaytest.usecase.vo.StockVo

object StockDetailDialog {
    fun show(context: Context, stock: StockVo): AlertDialog {
        val title = context.getString(
            R.string.dialog_title_stock_detail,
            DisplayStockUtil.textOrPlaceholder(stock.code),
            DisplayStockUtil.textOrPlaceholder(stock.name),
        )
        val body = buildString {
            appendField(context, R.string.label_pe_ratio, stock.peRatio)
            appendLine()
            appendField(
                context,
                R.string.label_dividend_yield,
                DisplayStockUtil.formatPercent(stock.dividendYield),
            )
            appendLine()
            appendField(context, R.string.label_pb_ratio, stock.pbRatio)

            val noRatioData = stock.peRatio.isNullOrBlank() &&
                stock.dividendYield.isNullOrBlank() &&
                stock.pbRatio.isNullOrBlank()
            if (noRatioData) {
                appendLine()
                appendLine()
                append(context.getString(R.string.hint_no_ratio_data))
            }
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton(R.string.action_dismiss, null)
            .show()
    }

    private fun StringBuilder.appendField(context: Context, labelRes: Int, value: String?) {
        append(context.getString(labelRes))
        append("  ")
        append(DisplayStockUtil.textOrPlaceholder(value))
    }
}
