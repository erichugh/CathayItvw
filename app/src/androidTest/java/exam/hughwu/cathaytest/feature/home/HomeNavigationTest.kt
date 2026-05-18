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
import exam.hughwu.cathaytest.common.network.NetworkStateProvider
import exam.hughwu.repository.twse.repo.ExchangeReportRepository
import exam.hughwu.retrofit.NetworkResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the Home entry page: the three variant buttons show and Navigation
 * routes into the XML and Hybrid destinations (then back) when online, and the
 * no-network prompt replaces them when offline.
 *
 * Repository is mocked so navigating into a variant (which sends `Init`)
 * doesn't hit the real TWSE API. [NetworkStateProvider] is faked via
 * `@BindValue` so connectivity is deterministic and independent of the test
 * device's real network (the old `NetworkStateManager.init()` is never called
 * under `HiltTestApplication`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val fakeRepo: ExchangeReportRepository = mockk(relaxed = true)

    @BindValue
    @JvmField
    val fakeNetwork: NetworkStateProvider = mockk(relaxed = true)

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

    private fun online() {
        every { fakeNetwork.isNetworkConnected() } returns true
        every { fakeNetwork.networkStateFlow } returns flowOf(true)
    }

    private fun offline() {
        every { fakeNetwork.isNetworkConnected() } returns false
        every { fakeNetwork.networkStateFlow } returns flowOf(false)
    }

    @Test
    fun home_shows_three_buttons() {
        online()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.btn_variant_xml)).check(matches(isDisplayed()))
            onView(withText(R.string.btn_variant_hybrid)).check(matches(isDisplayed()))
            onView(withText(R.string.btn_variant_compose)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun navigates_to_xml_then_back() {
        online()
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
        online()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.btn_variant_hybrid)).perform(click())
            onView(withText(R.string.title_variant_hybrid)).check(matches(isDisplayed()))
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))

            pressBack()
            onView(withText(R.string.btn_variant_xml)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun offline_hides_variant_buttons_and_shows_network_prompt() {
        offline()
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText(R.string.no_network_message)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_open_network_settings)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_xml)).check(matches(not(isDisplayed())))
        }
    }
}
