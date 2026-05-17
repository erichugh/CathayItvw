package exam.hughwu.cathaytest.feature.stocklist

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import exam.hughwu.cathaytest.MainActivity
import exam.hughwu.cathaytest.R
import exam.hughwu.repository.twse.repo.ExchangeReportRepository
import exam.hughwu.retrofit.NetworkResponse
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockDailyTrading
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockPriceAvg
import exam.hughwu.retrofit.twse.model.exchangereport.response.StockRatio
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Happy-path Espresso check that goes through the full stack:
 * MainActivity → HomeFragment → (tap "Hybrid") → StockListFragment →
 * ViewModel → UseCase → (mocked Repository).
 *
 * The start destination is now [exam.hughwu.cathaytest.feature.home.HomeFragment],
 * so the test first navigates into the Hybrid variant before asserting the list.
 * The repository is swapped via [BindValue] so we don't hit the real TWSE API.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StockListUiTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val fakeRepo: ExchangeReportRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        coEvery { fakeRepo.getStockDailyTradingReport() } returns NetworkResponse.Success(
            listOf(
                daily("2330", "台積電"),
                daily("0050", "元大台灣50"),
            )
        )
        coEvery { fakeRepo.getStockPriceAverageReport() } returns NetworkResponse.Success(
            listOf(avg("2330", "台積電"))
        )
        coEvery { fakeRepo.getStockEvaluationReport() } returns NetworkResponse.Success(
            listOf(ratio("2330", "台積電"))
        )
        hiltRule.inject()
    }

    @Test
    fun navigates_to_hybrid_and_renders_stock_list_with_filter_action() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Home entry page → tap the Hybrid button.
            onView(withText(R.string.btn_variant_hybrid)).check(matches(isDisplayed()))
            onView(withText(R.string.btn_variant_hybrid)).perform(click())

            // Hybrid screen toolbar title
            onView(withText(R.string.title_variant_hybrid)).check(matches(isDisplayed()))
            // Filter menu
            onView(withId(R.id.action_filter)).check(matches(isDisplayed()))
            // RecyclerView populated (poll within the same scenario)
            waitForRecyclerViewToHaveItems(scenario, R.id.recycler_view, expected = 2)
        }
    }

    private fun waitForRecyclerViewToHaveItems(
        scenario: ActivityScenario<MainActivity>,
        rvId: Int,
        expected: Int,
        timeoutMs: Long = 5_000,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var count = 0
        while (System.currentTimeMillis() < deadline) {
            scenario.onActivity { activity ->
                val rv = activity.findViewById<RecyclerView>(rvId)
                count = rv?.adapter?.itemCount ?: 0
            }
            if (count >= expected) return
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(150)
        }
        throw AssertionError("RecyclerView did not reach $expected items in $timeoutMs ms (got $count)")
    }

    private fun daily(code: String, name: String) = StockDailyTrading(
        change = "1.0", closingPrice = "100.0", code = code, date = "20260514",
        highestPrice = "101.0", lowestPrice = "99.0", name = name,
        openingPrice = "99.5", tradeValue = "1000", tradeVolume = "10",
        transaction = "5",
    )

    private fun avg(code: String, name: String) = StockPriceAvg(
        closingPrice = "100.0", code = code, date = "20260514",
        monthlyAveragePrice = "98.0", name = name,
    )

    private fun ratio(code: String, name: String) = StockRatio(
        code = code, date = "20260514", dividendYield = "3.0",
        name = name, pBratio = "1.5", pEratio = "12.0",
    )
}
