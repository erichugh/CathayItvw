package exam.hughwu.cathaytest.feature.home

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import exam.hughwu.cathaytest.MainActivity
import exam.hughwu.cathaytest.R
import exam.hughwu.repository.twse.repo.ExchangeReportRepository
import exam.hughwu.retrofit.NetworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the Home entry page shows the three variant buttons and that
 * Navigation routes into the XML and Hybrid destinations (then back).
 *
 * Repository is mocked so navigating into a variant (which sends `Init`)
 * doesn't hit the real TWSE API.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val fakeRepo: ExchangeReportRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        coEvery { fakeRepo.getStockDailyTradingReport() } returns
            NetworkResponse.Success(emptyList())
        coEvery { fakeRepo.getStockPriceAverageReport() } returns
            NetworkResponse.Success(emptyList())
        coEvery { fakeRepo.getStockEvaluationReport() } returns
            NetworkResponse.Success(emptyList())
        hiltRule.inject()
    }

    @Test
    fun home_shows_three_buttons() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.btn_variant_xml)).check(matches(isDisplayed()))
            onView(withText(R.string.btn_variant_hybrid)).check(matches(isDisplayed()))
            onView(withText(R.string.btn_variant_compose)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun navigates_to_xml_then_back() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.btn_variant_xml)).perform(click())
            onView(withText(R.string.title_variant_xml)).check(matches(isDisplayed()))
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))

            pressBack()
            onView(withText(R.string.btn_variant_hybrid)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun navigates_to_hybrid_then_back() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.btn_variant_hybrid)).perform(click())
            onView(withText(R.string.title_variant_hybrid)).check(matches(isDisplayed()))
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))

            pressBack()
            onView(withText(R.string.btn_variant_xml)).check(matches(isDisplayed()))
        }
    }
}
