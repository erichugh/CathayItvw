package exam.hughwu.cathaytest.feature.stocklist.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil
import exam.hughwu.cathaytest.usecase.vo.StockVo

object StockDetailDialog {
    fun show(context: Context, stock: StockVo): AlertDialog {
        val title = DisplayStockUtil.formatStockDetailTitle(
            context = context,
            stockCode = stock.code,
            stockName = stock.name
        )

        val body = DisplayStockUtil.formatStockDetailBody(
            context = context,
            peRatio = stock.peRatio,
            dividendYield = stock.dividendYield,
            pbRatio = stock.pbRatio,
        )

        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton(R.string.action_dismiss, null)
            .show()
    }
}
