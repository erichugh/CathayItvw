package exam.hughwu.cathaytest.feature.stocklist.vo

import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil
import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil.PriceTrend
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayStockTest {

    // -- closingVsAverage -------------------------------------------------

    @Test fun `closing higher than avg returns UP`() {
        assertEquals(PriceTrend.UP, DisplayStockUtil.closingVsAverage("110.5", "100.0"))
    }

    @Test fun `closing lower than avg returns DOWN`() {
        assertEquals(PriceTrend.DOWN, DisplayStockUtil.closingVsAverage("90.0", "100.0"))
    }

    @Test fun `equal closing and avg returns FLAT`() {
        assertEquals(PriceTrend.FLAT, DisplayStockUtil.closingVsAverage("100.0", "100.00"))
    }

    @Test fun `null inputs return UNKNOWN`() {
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.closingVsAverage(null, "100"))
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.closingVsAverage("100", null))
    }

    @Test fun `unparsable inputs return UNKNOWN`() {
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.closingVsAverage("--", "100"))
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.closingVsAverage("100", "abc"))
    }

    /**
     * Real-world pattern from STOCK_DAY_AVG_ALL: 7049/23640 rows have an empty-string
     * ClosingPrice but a populated MonthlyAveragePrice. Must not crash, must show UNKNOWN.
     */
    @Test fun `empty closing with populated average is UNKNOWN`() {
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.closingVsAverage("", "0.46"))
    }

    // -- changeTrend ------------------------------------------------------

    @Test fun `positive change is UP, negative is DOWN, zero is FLAT`() {
        assertEquals(PriceTrend.UP, DisplayStockUtil.changeTrend("1.20"))
        assertEquals(PriceTrend.DOWN, DisplayStockUtil.changeTrend("-0.85"))
        assertEquals(PriceTrend.FLAT, DisplayStockUtil.changeTrend("0.00"))
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.changeTrend(null))
        assertEquals(PriceTrend.UNKNOWN, DisplayStockUtil.changeTrend("--"))
    }

    /** TWSE Change field arrives with 4 decimal places: "0.0000" / "-0.5000" etc. */
    @Test fun `changeTrend handles 4-decimal-place format from TWSE`() {
        assertEquals(PriceTrend.FLAT, DisplayStockUtil.changeTrend("0.0000"))
        assertEquals(PriceTrend.UP, DisplayStockUtil.changeTrend("0.0500"))
        assertEquals(PriceTrend.DOWN, DisplayStockUtil.changeTrend("-0.5000"))
    }

    // -- formatters -------------------------------------------------------

    @Test fun `formatNumber adds thousands separators`() {
        assertEquals("1,234", DisplayStockUtil.formatNumber("1234"))
        assertEquals("1,234.56", DisplayStockUtil.formatNumber("1234.56"))
        assertEquals("--", DisplayStockUtil.formatNumber(null))
        assertEquals("--", DisplayStockUtil.formatNumber("not-a-number"))
    }

    /**
     * Real TradeValue can hit 12 digits (台積電 ~121 billion).
     * Make sure we parse to Long, not Int, and the comma format scales.
     */
    @Test fun `formatNumber handles 12-digit TradeValue without overflow`() {
        assertEquals("121,891,443,938", DisplayStockUtil.formatNumber("121891443938"))
        assertEquals("4,204,257,454", DisplayStockUtil.formatNumber("4204257454")) // > Int.MAX_VALUE
    }

    @Test fun `formatSignedNumber prepends sign`() {
        assertEquals("+1.20", DisplayStockUtil.formatSignedNumber("1.20"))
        assertEquals("-0.85", DisplayStockUtil.formatSignedNumber("-0.85"))
        assertEquals("0.00", DisplayStockUtil.formatSignedNumber("0"))
        assertEquals("--", DisplayStockUtil.formatSignedNumber(null))
    }

    @Test fun `textOrPlaceholder returns dashes for blank`() {
        assertEquals("台積電", DisplayStockUtil.textOrPlaceholder("台積電"))
        assertEquals("--", DisplayStockUtil.textOrPlaceholder(""))
        assertEquals("--", DisplayStockUtil.textOrPlaceholder("  "))
        assertEquals("--", DisplayStockUtil.textOrPlaceholder(null))
    }

    @Test fun `formatPercent wraps numeric values and returns -- for missing or invalid`() {
        assertEquals("(1.18%)", DisplayStockUtil.formatPercent("1.18"))
        assertEquals("(0%)", DisplayStockUtil.formatPercent("0"))
        assertEquals("--", DisplayStockUtil.formatPercent(null))
        assertEquals("--", DisplayStockUtil.formatPercent(""))
        assertEquals("--", DisplayStockUtil.formatPercent("  "))
        assertEquals("--", DisplayStockUtil.formatPercent("--"))
        assertEquals("--", DisplayStockUtil.formatPercent("N/A"))
    }
}
