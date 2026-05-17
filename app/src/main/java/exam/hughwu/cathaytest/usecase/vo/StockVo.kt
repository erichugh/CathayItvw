package exam.hughwu.cathaytest.usecase.vo

import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio

/**
 * Domain model produced by [exam.hughwu.cathaytest.usecase.GetMergedStocksUseCase]
 * after merging the three TWSE endpoints by `Code`.
 *
 * All optional fields are nullable; UI converts `null` (and parse failures)
 * to "--" at render time.
 */
data class StockVo(
    val code: String,
    val name: String?,
    // from STOCK_DAY_ALL
    val openingPrice: String?,
    val highestPrice: String?,
    val lowestPrice: String?,
    val closingPrice: String?,
    val change: String?,
    val tradeVolume: String?,
    val tradeValue: String?,
    val transaction: String?,
    // from STOCK_DAY_AVG_ALL
    val monthlyAveragePrice: String?,
    // from BWIBBU_ALL
    val peRatio: String?,
    val dividendYield: String?,
    val pbRatio: String?,
) {
    companion object {
        /**
         * Build a [StockVo] from the (possibly partial) trio.
         *
         * At least one of the three must carry a non-null `code`; otherwise
         * the caller has no business invoking us.
         */
        fun from(
            day: StockDailyTrading?,
            avg: StockPriceAvg?,
            ratio: StockRatio?,
        ): StockVo {
            val code = day?.code ?: avg?.code ?: ratio?.code
            requireNotNull(code) { "StockVo.from invoked with all three null codes" }
            val name = day?.name ?: avg?.name ?: ratio?.name
            return StockVo(
                code = code,
                name = name,
                openingPrice = day?.openingPrice,
                highestPrice = day?.highestPrice,
                lowestPrice = day?.lowestPrice,
                closingPrice = day?.closingPrice,
                change = day?.change,
                tradeVolume = day?.tradeVolume,
                tradeValue = day?.tradeValue,
                transaction = day?.transaction,
                monthlyAveragePrice = avg?.monthlyAveragePrice,
                peRatio = ratio?.pEratio,
                dividendYield = ratio?.dividendYield,
                pbRatio = ratio?.pBratio,
            )
        }
    }
}
