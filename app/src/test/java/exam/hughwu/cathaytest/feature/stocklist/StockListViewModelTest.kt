package exam.hughwu.cathaytest.feature.stocklist

import app.cash.turbine.test
import exam.hughwu.cathaytest.feature.stocklist.StockListUiState.SortOrder
import exam.hughwu.cathaytest.usecase.GetMergedStocksUseCase
import exam.hughwu.cathaytest.usecase.vo.StockVo
import exam.hughwu.repository.preferences.SortPreferenceRepository
import exam.hughwu.repository.preferences.StoredSortOrder
import exam.hughwu.retrofit.UseCaseResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: GetMergedStocksUseCase
    private lateinit var sortPref: SortPreferenceRepository
    private lateinit var sortFlow: MutableStateFlow<StoredSortOrder>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        sortPref = mockk(relaxed = true)
        sortFlow = MutableStateFlow(StoredSortOrder.CODE_DESC)
        coEvery { sortPref.sortOrderFlow } returns sortFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Init loads stocks and sorts descending by code`() = runTest {
        every { useCase() } returns flowOf(
            UseCaseResponse.Success(listOf(stock("2330"), stock("0050"), stock("9999")))
        )

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        assertEquals(
            listOf("9999", "2330", "0050"),
            vm.uiState.value.stocks.map { it.code },
        )
        assertEquals(SortOrder.CodeDesc, vm.uiState.value.sortOrder)
    }

    @Test
    fun `OnSortSelected with different order persists to DataStore`() = runTest {
        every { useCase() } returns flowOf(UseCaseResponse.Success(emptyList()))

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        vm.sendIntent(StockListIntent.OnSortSelected(SortOrder.CodeAsc))
        advanceUntilIdle()

        coVerify(exactly = 1) { sortPref.setSortOrder(StoredSortOrder.CODE_ASC) }
    }

    @Test
    fun `OnSortSelected with same order is a no-op (no write, no scroll event)`() = runTest {
        every { useCase() } returns flowOf(UseCaseResponse.Success(emptyList()))

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        // Initial sortOrder is CodeDesc (from sortFlow default). Pick the same.
        vm.sendIntent(StockListIntent.OnSortSelected(SortOrder.CodeDesc))
        advanceUntilIdle()

        coVerify(exactly = 0) { sortPref.setSortOrder(any()) }
    }

    @Test
    fun `DataStore emission flips list to ascending`() = runTest {
        every { useCase() } returns flowOf(
            UseCaseResponse.Success(listOf(stock("2330"), stock("0050"), stock("9999")))
        )

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        sortFlow.value = StoredSortOrder.CODE_ASC
        advanceUntilIdle()

        assertEquals(
            listOf("0050", "2330", "9999"),
            vm.uiState.value.stocks.map { it.code },
        )
        assertEquals(SortOrder.CodeAsc, vm.uiState.value.sortOrder)
    }

    @Test
    fun `OnItemClicked emits ShowDetailDialog event with the right stock`() = runTest {
        every { useCase() } returns flowOf(
            UseCaseResponse.Success(listOf(stock("2330")))
        )

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        vm.uiEvent.test {
            vm.sendIntent(StockListIntent.OnItemClicked("2330"))
            val event = awaitItem()
            assertEquals(true, event is StockListEvent.ShowDetailDialog)
            assertEquals("2330", (event as StockListEvent.ShowDetailDialog).stock.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSortIconClicked emits ShowSortSheet with current order`() = runTest {
        every { useCase() } returns flowOf(UseCaseResponse.Success(emptyList()))

        val vm = StockListViewModel(useCase, sortPref)
        vm.sendIntent(StockListIntent.Init)
        advanceUntilIdle()

        vm.uiEvent.test {
            vm.sendIntent(StockListIntent.OnSortIconClicked)
            val event = awaitItem()
            assertEquals(true, event is StockListEvent.ShowSortSheet)
            assertEquals(SortOrder.CodeDesc, (event as StockListEvent.ShowSortSheet).current)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun stock(code: String): StockVo = StockVo(
        code = code,
        name = "name-$code",
        openingPrice = "10.0",
        highestPrice = "11.0",
        lowestPrice = "9.0",
        closingPrice = "10.5",
        change = "0.5",
        tradeVolume = "100",
        tradeValue = "1000",
        transaction = "10",
        monthlyAveragePrice = "10.2",
        peRatio = "12.0",
        dividendYield = "3.0",
        pbRatio = "1.5",
    )
}
