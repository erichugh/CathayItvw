package exam.hughwu.cathaytest.feature.stocklist.vo

import java.text.NumberFormat
import java.util.Locale

/**
 * Pure helpers for translating raw StockVo
 * fields into UI-ready values.
 */
object DisplayStockUtil {

    const val PLACEHOLDER = "--"

    enum class PriceTrend { UP, DOWN, FLAT, UNKNOWN }

    /**
     * Compare closing price with monthly average. UP → 紅, DOWN → 綠, FLAT → 灰.
     */
    fun closingVsAverage(closing: String?, average: String?): PriceTrend {
        val c = closing?.toDoubleOrNull() ?: return PriceTrend.UNKNOWN
        val a = average?.toDoubleOrNull() ?: return PriceTrend.UNKNOWN
        return when {
            c > a -> PriceTrend.UP
            c < a -> PriceTrend.DOWN
            else -> PriceTrend.FLAT
        }
    }

    /**
     * Trend for "漲跌價差". Positive → 紅, Negative → 綠, Zero → 灰.
     */
    fun changeTrend(change: String?): PriceTrend {
        val v = change?.toDoubleOrNull() ?: return PriceTrend.UNKNOWN
        return when {
            v > 0 -> PriceTrend.UP
            v < 0 -> PriceTrend.DOWN
            else -> PriceTrend.FLAT
        }
    }

    /** Format a numeric string with thousands separators. Falls back to "--". */
    fun formatNumber(raw: String?): String {
        val parsed = raw?.toDoubleOrNull() ?: return PLACEHOLDER
        // Keep 2 decimals only if the source had a decimal point.
        return if (raw.contains('.')) {
            String.format(Locale.US, "%,.2f", parsed)
        } else {
            INT_FORMAT.format(parsed.toLong())
        }
    }

    /** Format with explicit +/- prefix for change-style fields. */
    fun formatSignedNumber(raw: String?): String {
        val parsed = raw?.toDoubleOrNull() ?: return PLACEHOLDER
        val abs = String.format(Locale.US, "%,.2f", kotlin.math.abs(parsed))
        return when {
            parsed > 0 -> "+$abs"
            parsed < 0 -> "-$abs"
            else -> abs
        }
    }

    /** Plain text fallback: returns input or "--" when null/blank. */
    fun textOrPlaceholder(raw: String?): String =
        if (raw.isNullOrBlank()) PLACEHOLDER else raw

    /**
     * Wrap a numeric string in `(value%)` for percentage display.
     * Returns "--" for null, blank, or non-numeric input — so callers don't
     * need to special-case missing data themselves.
     */
    fun formatPercent(raw: String?): String {
        if (raw.isNullOrBlank()) return PLACEHOLDER
        if (raw.toDoubleOrNull() == null) return PLACEHOLDER
        return "($raw%)"
    }

    private val INT_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
}
