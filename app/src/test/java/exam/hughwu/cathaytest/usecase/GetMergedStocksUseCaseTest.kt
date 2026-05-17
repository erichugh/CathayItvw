package exam.hughwu.cathaytest.usecase

import exam.hughwu.repository.twse.repo.ExchangeReportRepository
import exam.hughwu.retrofit.ApiError
import exam.hughwu.retrofit.NetworkResponse
import exam.hughwu.retrofit.UseCaseResponse
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMergedStocksUseCaseTest {

    private lateinit var repo: ExchangeReportRepository
    private lateinit var useCase: GetMergedStocksUseCase

    @Before
    fun setUp() {
        repo = mockk()
        useCase = GetMergedStocksUseCase(repo)
    }

    @Test
    fun `all three endpoints succeed - final emission contains union of codes`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(
            listOf(daily("2330"), daily("2317"))
        )
        coEvery { repo.getStockPriceAverageReport() } returns success(
            listOf(avg("2330"), avg("0050"))
        )
        coEvery { repo.getStockEvaluationReport() } returns success(
            listOf(ratio("2330"))
        )

        val emissions = useCase().toList()

        // Phase 1 (day+avg) then phase 2 (ratios) => 2 Success emissions.
        assertEquals(2, emissions.size)
        assertTrue(emissions.all { it is UseCaseResponse.Success })
        val finalCodes = (emissions.last() as UseCaseResponse.Success).data!!.map { it.code }.toSet()
        assertEquals(setOf("2330", "2317", "0050"), finalCodes)
    }

    /**
     * Core requirement: the list must render once DAY *and* AVG are in,
     * WITHOUT waiting for the slower BWIBBU. BWIBBU then refines the list.
     */
    @Test
    fun `list emits after day and avg, BWIBBU refines later`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(listOf(daily("2330")))
        coEvery { repo.getStockPriceAverageReport() } returns success(listOf(avg("2330")))
        coEvery { repo.getStockEvaluationReport() } coAnswers {
            delay(1_000) // BWIBBU is the slow one
            success(listOf(ratio("2330")))
        }

        val emissions = useCase().toList()

        assertEquals(2, emissions.size)

        // Phase 1: day + avg columns present, ratio columns still null.
        val first = (emissions.first() as UseCaseResponse.Success).data!!.single()
        assertEquals("2330", first.code)
        assertEquals("100.0", first.closingPrice)          // from DAY
        assertEquals("550.0", first.monthlyAveragePrice)   // from AVG
        assertNull(first.peRatio)
        assertNull(first.pbRatio)
        assertNull(first.dividendYield)

        // Phase 2: BWIBBU folded in.
        val second = (emissions.last() as UseCaseResponse.Success).data!!.single()
        assertEquals("12.5", second.peRatio)
        assertEquals("3.5", second.dividendYield)
        assertEquals("1.2", second.pbRatio)
    }

    @Test
    fun `daily fails - still success with null daily fields in final emission`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns failure()
        coEvery { repo.getStockPriceAverageReport() } returns success(listOf(avg("2330")))
        coEvery { repo.getStockEvaluationReport() } returns success(listOf(ratio("2330")))

        val emissions = useCase().toList()
        val finalSuccess = emissions.last() as UseCaseResponse.Success
        val stock = finalSuccess.data!!.single()

        assertEquals("2330", stock.code)
        assertNull(stock.openingPrice)
        assertNull(stock.closingPrice)
        assertEquals("550.0", stock.monthlyAveragePrice)
        assertEquals("12.5", stock.peRatio)
    }

    @Test
    fun `all three fail - emits exactly one Failure`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns failure()
        coEvery { repo.getStockPriceAverageReport() } returns failure()
        coEvery { repo.getStockEvaluationReport() } returns failure()

        val emissions = useCase().toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions.single() is UseCaseResponse.Failure)
    }

    @Test
    fun `entries with null or blank code are dropped`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(
            listOf(daily("2330"), daily(null), daily(""))
        )
        coEvery { repo.getStockPriceAverageReport() } returns success(emptyList())
        coEvery { repo.getStockEvaluationReport() } returns success(emptyList())

        val emissions = useCase().toList()
        val finalSuccess = emissions.last() as UseCaseResponse.Success
        val codes = finalSuccess.data!!.map { it.code }
        assertEquals(listOf("2330"), codes)
    }

    /**
     * Real-world: STOCK_DAY_AVG_ALL has ~22k codes (warrants/derivatives) that don't
     * appear in DAY or BWIBBU. Those should still surface as StockVo with
     * day/ratio fields all null.
     */
    @Test
    fun `code present only in avg endpoint surfaces as warrant-style entry`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(emptyList())
        coEvery { repo.getStockPriceAverageReport() } returns success(
            listOf(avg("030012"))
        )
        coEvery { repo.getStockEvaluationReport() } returns success(emptyList())

        val emissions = useCase().toList()
        val finalSuccess = emissions.last() as UseCaseResponse.Success
        val warrant = finalSuccess.data!!.single()
        assertEquals("030012", warrant.code)
        assertEquals("550.0", warrant.monthlyAveragePrice)
        // No DAY → all trading fields null
        assertNull(warrant.openingPrice)
        assertNull(warrant.tradeVolume)
        assertNull(warrant.change)
        // No BWIBBU → all ratios null
        assertNull(warrant.peRatio)
        assertNull(warrant.dividendYield)
        assertNull(warrant.pbRatio)
    }

    /**
     * Real-world: 2 codes in our sample appear in DAY but not in AVG/BWIBBU.
     * Union should still include them with avg/ratio fields null.
     */
    @Test
    fun `code present only in day endpoint still surfaces`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(
            listOf(daily("9999X"))
        )
        coEvery { repo.getStockPriceAverageReport() } returns success(emptyList())
        coEvery { repo.getStockEvaluationReport() } returns success(emptyList())

        val emissions = useCase().toList()
        val finalSuccess = emissions.last() as UseCaseResponse.Success
        val orphan = finalSuccess.data!!.single()
        assertEquals("9999X", orphan.code)
        assertEquals("100.0", orphan.closingPrice)
        assertNull(orphan.monthlyAveragePrice)
        assertNull(orphan.peRatio)
    }

    /**
     * Real-world: PEratio / DividendYield / ClosingPrice are commonly empty strings,
     * not null (251/1074, 188/1074, 7049/23640 in our sample). Make sure such codes
     * still merge correctly and the empty-string values flow through to the StockVo
     * unchanged so the UI can render them as "--".
     */
    @Test
    fun `empty-string fields propagate (UI converts to dashes downstream)`() = runTest {
        coEvery { repo.getStockDailyTradingReport() } returns success(
            listOf(daily("1101").copy(closingPrice = ""))
        )
        coEvery { repo.getStockPriceAverageReport() } returns success(
            listOf(avg("1101").copy(closingPrice = ""))
        )
        coEvery { repo.getStockEvaluationReport() } returns success(
            listOf(ratio("1101").copy(pEratio = "", dividendYield = ""))
        )

        val emissions = useCase().toList()
        val finalSuccess = emissions.last() as UseCaseResponse.Success
        val stock = finalSuccess.data!!.single()
        assertEquals("1101", stock.code)
        assertEquals("", stock.closingPrice)
        assertEquals("", stock.peRatio)
        assertEquals("", stock.dividendYield)
        // Non-empty fields still pass through.
        assertEquals("1.2", stock.pbRatio)
    }

    // -- factories ---------------------------------------------------------

    private fun <T : Any> success(body: T): NetworkResponse<T, ApiError> =
        NetworkResponse.Success(body)

    private fun failure(): NetworkResponse<Nothing, ApiError> =
        NetworkResponse.Failure(ApiError(message = "fail"))

    private fun daily(code: String?) = StockDailyTrading(
        change = "1.0",
        closingPrice = "100.0",
        code = code,
        date = "20260514",
        highestPrice = "101.0",
        lowestPrice = "99.0",
        name = "Foo",
        openingPrice = "99.5",
        tradeValue = "1000",
        tradeVolume = "10",
        transaction = "5",
    )

    private fun avg(code: String) = StockPriceAvg(
        closingPrice = "100.0",
        code = code,
        date = "20260514",
        monthlyAveragePrice = "550.0",
        name = "Bar",
    )

    private fun ratio(code: String) = StockRatio(
        code = code,
        date = "20260514",
        dividendYield = "3.5",
        name = "Baz",
        pBratio = "1.2",
        pEratio = "12.5",
    )
}
