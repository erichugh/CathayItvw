package exam.hughwu.retrofit.twse.api

import exam.hughwu.retrofit.GenericResponse
import exam.hughwu.retrofit.RestfulWebServicePaths.STOCK_RATIOS_ALL
import exam.hughwu.retrofit.RestfulWebServicePaths.STOCK_PRICE_AVG_ALL
import exam.hughwu.retrofit.RestfulWebServicePaths.STOCK_DAILY_TRADING_ALL
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio
import retrofit2.http.GET

interface ExchangeReportAPI {
    /**
     * 取得上市個股日本益比、殖利率及股價淨值比
     */
    @GET(STOCK_RATIOS_ALL)
    suspend fun getStockEvaluationReport(): GenericResponse<List<StockRatio>>

    /**
     * 取得上市個股日收盤價及月平均價
     */
    @GET(STOCK_PRICE_AVG_ALL)
    suspend fun getStockPriceAverageReport(): GenericResponse<List<StockPriceAvg>>

    /**
     * 取得上市個股日成交資訊 (開高低收量)
     */
    @GET(STOCK_DAILY_TRADING_ALL)
    suspend fun getStockDailyTradingReport(): GenericResponse<List<StockDailyTrading>>
}