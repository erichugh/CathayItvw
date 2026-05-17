package exam.hughwu.retrofit

object RestfulWebServicePaths {
    const val EXCHANGE_REPORT = "exchangeReport"
    // 上市個股日本益比、殖利率及股價淨值比
    const val STOCK_RATIOS_ALL = "${EXCHANGE_REPORT}/BWIBBU_ALL"
    // 上市個股日收盤價及月平均價
    const val STOCK_PRICE_AVG_ALL = "${EXCHANGE_REPORT}/STOCK_DAY_AVG_ALL"
    // 上市個股日成交資訊
    const val STOCK_DAILY_TRADING_ALL = "${EXCHANGE_REPORT}/STOCK_DAY_ALL"
}