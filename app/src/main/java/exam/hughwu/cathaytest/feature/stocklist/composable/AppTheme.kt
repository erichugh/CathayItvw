package exam.hughwu.cathaytest.feature.stocklist.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil

data class TrendColors(
    val up: Color,
    val down: Color,
    val flat: Color,
)

val LocalTrendColors = staticCompositionLocalOf<TrendColors> {
    error("LocalTrendColors not provided. Wrap content in AppTheme { }.")
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    val colors = base.copy(
        primary = colorResource(R.color.scheme_primary),
        onPrimary = colorResource(R.color.scheme_on_primary),
        primaryContainer = colorResource(R.color.scheme_primary_container),
        onPrimaryContainer = colorResource(R.color.scheme_on_primary_container),
        surface = colorResource(R.color.scheme_surface),
        onSurface = colorResource(R.color.scheme_on_surface),
        surfaceVariant = colorResource(R.color.scheme_surface_variant),
        onSurfaceVariant = colorResource(R.color.scheme_on_surface_variant),
        background = colorResource(R.color.scheme_background),
        onBackground = colorResource(R.color.scheme_on_background),
    )
    val trend = TrendColors(
        up = colorResource(R.color.trend_up),
        down = colorResource(R.color.trend_down),
        flat = colorResource(R.color.trend_flat),
    )
    CompositionLocalProvider(LocalTrendColors provides trend) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

@Composable
@ReadOnlyComposable
fun trendColor(trend: DisplayStockUtil.PriceTrend): Color {
    val palette = LocalTrendColors.current
    return when (trend) {
        DisplayStockUtil.PriceTrend.UP -> palette.up
        DisplayStockUtil.PriceTrend.DOWN -> palette.down
        DisplayStockUtil.PriceTrend.FLAT -> palette.flat
        DisplayStockUtil.PriceTrend.UNKNOWN -> MaterialTheme.colorScheme.onSurface
    }
}
