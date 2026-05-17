package exam.hughwu.cathaytest.usecase

import exam.hughwu.cathaytest.usecase.vo.StockVo
import exam.hughwu.repository.twse.repo.ExchangeReportRepository
import exam.hughwu.retrofit.ApiError
import exam.hughwu.retrofit.NetworkResponse
import exam.hughwu.retrofit.UseCaseResponse
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

/**
 * Fetches the three TWSE endpoints in parallel and emits a merged stock list
 * in (up to) two phases:
 *
 * 1. **List phase** — once `STOCK_DAY_ALL` *and* `STOCK_DAY_AVG_ALL` have both
 *    returned, emit a [UseCaseResponse.Success] containing the merged list
 *    *without* ratio data. Even if `BWIBBU_ALL` happens to have finished by
 *    this point, its data is intentionally withheld so the list shape
 *    stabilises before ratios fill in.
 * 2. **Ratio phase** — when `BWIBBU_ALL` returns, emit a second
 *    [UseCaseResponse.Success] with PE/dividend/PB merged in. If BWIBBU brings
 *    no data and phase 1 already emitted, we skip the second emit.
 *
 * Error policy: partial success acceptable. Only when all three endpoints
 * fail/return empty do we emit a single [UseCaseResponse.Failure].
 *
 * Emission contract:
 * - Phase 1 emits iff DAY or AVG carry data. Skipped when both are empty.
 * - Phase 2 emits iff RATIO carries data, OR phase 1 was skipped (single emit
 *   carrying RATIO-only data, or Failure if RATIO was also empty).
 */
class GetMergedStocksUseCase @Inject constructor(
    private val repo: ExchangeReportRepository,
) {
    operator fun invoke(): Flow<UseCaseResponse<List<StockVo>>> = channelFlow {
        val dayDef = async { fetchDayMap() }
        val avgDef = async { fetchAvgMap() }
        val ratioDef = async { fetchRatioMap() }

        // Phase 1: list waits for BOTH day AND avg. BWIBBU is excluded here even
        // if it finished early — phase 2 handles all ratio rendering.
        val day = dayDef.await()
        val avg = avgDef.await()

        val phase1HasData = day.isNotEmpty() || avg.isNotEmpty()
        if (phase1HasData) {
            send(UseCaseResponse.Success(buildMerged(day, avg, emptyMap())))
        }

        // Phase 2: BWIBBU's turn. May arrive after phase 1 (typical) or have
        // been ready already — either way we await it here.
        val ratio = ratioDef.await()

        when {
            // BWIBBU brought data — emit refined list with ratios filled in.
            ratio.isNotEmpty() -> {
                send(UseCaseResponse.Success(buildMerged(day, avg, ratio)))
            }
            // All three endpoints came back empty → Failure.
            !phase1HasData -> {
                send(UseCaseResponse.Failure(ApiError(message = "All TWSE endpoints failed")))
            }
            // else: phase 1 already showed the list, BWIBBU added nothing → no second emit.
        }
    }

    private suspend fun fetchDayMap(): Map<String, StockDailyTrading> =
        (repo.getStockDailyTradingReport() as? NetworkResponse.Success)?.body
            .orEmpty()
            .filter { !it.code.isNullOrBlank() }
            .associateBy { it.code!! }

    private suspend fun fetchAvgMap(): Map<String, StockPriceAvg> =
        (repo.getStockPriceAverageReport() as? NetworkResponse.Success)?.body
            .orEmpty()
            .filter { !it.code.isNullOrBlank() }
            .associateBy { it.code!! }

    private suspend fun fetchRatioMap(): Map<String, StockRatio> =
        (repo.getStockEvaluationReport() as? NetworkResponse.Success)?.body
            .orEmpty()
            .filter { !it.code.isNullOrBlank() }
            .associateBy { it.code!! }

    private fun buildMerged(
        day: Map<String, StockDailyTrading>,
        avg: Map<String, StockPriceAvg>,
        ratio: Map<String, StockRatio>,
    ): List<StockVo> {
        val codes = day.keys + avg.keys + ratio.keys
        return codes.map { code ->
            StockVo.from(
                day = day[code],
                avg = avg[code],
                ratio = ratio[code],
            )
        }
    }
}
