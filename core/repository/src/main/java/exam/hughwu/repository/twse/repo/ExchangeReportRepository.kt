package exam.hughwu.repository.twse.repo

import exam.hughwu.retrofit.GenericResponse
import exam.hughwu.retrofit.twse.api.ExchangeReportAPI
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExchangeReportRepository @Inject constructor(
    private val exchangeReportAPI: ExchangeReportAPI,
) {
    suspend fun getStockEvaluationReport(): GenericResponse<List<StockRatio>> {
        return withContext(Dispatchers.IO) {
            exchangeReportAPI.getStockEvaluationReport()
        }
    }

    suspend fun getStockPriceAverageReport(): GenericResponse<List<StockPriceAvg>> {
        return withContext(Dispatchers.IO) {
            exchangeReportAPI.getStockPriceAverageReport()
        }
    }

    suspend fun getStockDailyTradingReport(): GenericResponse<List<StockDailyTrading>> {
        return withContext(Dispatchers.IO) {
            exchangeReportAPI.getStockDailyTradingReport()
        }
    }
}