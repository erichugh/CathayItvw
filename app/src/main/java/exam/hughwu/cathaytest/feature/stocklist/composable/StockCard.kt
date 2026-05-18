package exam.hughwu.cathaytest.feature.stocklist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import exam.hughwu.cathaytest.R
import exam.hughwu.cathaytest.feature.stocklist.DisplayStockUtil
import exam.hughwu.cathaytest.usecase.vo.StockVo

@Composable
fun StockCard(
    stock: StockVo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val closingTrendColor = trendColor(
        DisplayStockUtil.closingVsAverage(stock.closingPrice, stock.monthlyAveragePrice)
    )
    val changeColor = trendColor(DisplayStockUtil.changeTrend(stock.change))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Code + Name
            Text(
                text = DisplayStockUtil.textOrPlaceholder(stock.code),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = DisplayStockUtil.textOrPlaceholder(stock.name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(10.dp))


            Column(
                modifier = Modifier.padding(start = 50.dp)
            ) {
                PriceRow(
                    leftLabel = stringResource(R.string.label_opening_price),
                    leftValue = DisplayStockUtil.formatNumber(stock.openingPrice),
                    leftColor = MaterialTheme.colorScheme.onSurface,
                    rightLabel = stringResource(R.string.label_closing_price),
                    rightValue = DisplayStockUtil.formatNumber(stock.closingPrice),
                    rightColor = closingTrendColor,
                )
                PriceRow(
                    leftLabel = stringResource(R.string.label_highest_price),
                    leftValue = DisplayStockUtil.formatNumber(stock.highestPrice),
                    leftColor = MaterialTheme.colorScheme.onSurface,
                    rightLabel = stringResource(R.string.label_lowest_price),
                    rightValue = DisplayStockUtil.formatNumber(stock.lowestPrice),
                    rightColor = MaterialTheme.colorScheme.onSurface,
                )
                PriceRow(
                    leftLabel = stringResource(R.string.label_change),
                    leftValue = DisplayStockUtil.formatSignedNumber(stock.change),
                    leftColor = changeColor,
                    rightLabel = stringResource(R.string.label_monthly_avg),
                    rightValue = DisplayStockUtil.formatNumber(stock.monthlyAveragePrice),
                    rightColor = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            VolumeRow(
                transaction = DisplayStockUtil.formatNumber(stock.transaction),
                volume = DisplayStockUtil.formatNumber(stock.tradeVolume),
                value = DisplayStockUtil.formatNumber(stock.tradeValue),
            )
        }
    }
}

@Composable
private fun PriceRow(
    leftLabel: String,
    leftValue: String,
    leftColor: Color,
    rightLabel: String,
    rightValue: String,
    rightColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabeledValue(leftLabel, leftValue, leftColor, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(20.dp))
        LabeledValue(rightLabel, rightValue, rightColor, modifier = Modifier.weight(1f))
    }
}

/**
 * Label pinned to the start, then a flexible [Spacer] that absorbs the gap,
 * leaving the value (and any future trailing element) hugging the end of the
 * column — matches the Figma spec where the numbers form a clean right-hand
 * column.
 */
@Composable
private fun LabeledValue(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Flexible gap: keeps label at the start and pushes the value (plus any
        // additional trailing component added later) to the end.
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun VolumeRow(
    transaction: String,
    volume: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VolumeCell(
            label = stringResource(R.string.label_transaction),
            value = transaction,
            modifier = Modifier.weight(0.85f),
        )
        VolumeCell(
            label = stringResource(R.string.label_trade_volume),
            value = volume,
            modifier = Modifier.weight(1f),
        )
        VolumeCell(
            label = stringResource(R.string.label_trade_value),
            value = value,
            modifier = Modifier.weight(1.3f),
        )
    }
}

/**
 * Label and value side-by-side (horizontal), per the Figma spec. Real TWSE
 * values run long (e.g. 成交金額 has 12+ digits) so the value is kept to a
 * single line and is allowed to scale down rather than wrap.
 */
@Composable
private fun VolumeCell(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStockCard() {
    AppTheme {
        Surface {
            StockCard(
                stock = StockVo(
                    code = "2330",
                    name = "台積電",
                    openingPrice = "1180.00",
                    highestPrice = "1185.00",
                    lowestPrice = "1175.00",
                    closingPrice = "1182.00",
                    change = "12.00",
                    tradeVolume = "12345678",
                    tradeValue = "14567890123",
                    transaction = "78901",
                    monthlyAveragePrice = "1100.00",
                    peRatio = "22.5",
                    dividendYield = "1.85",
                    pbRatio = "5.6",
                ),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStockCardMissingFields() {
    AppTheme {
        Surface {
            StockCard(
                stock = StockVo(
                    code = "0050",
                    name = null,
                    openingPrice = null,
                    highestPrice = null,
                    lowestPrice = null,
                    closingPrice = "200.00",
                    change = "-1.50",
                    tradeVolume = null,
                    tradeValue = null,
                    transaction = null,
                    monthlyAveragePrice = "210.00",
                    peRatio = null,
                    dividendYield = null,
                    pbRatio = null,
                ),
                onClick = {},
            )
        }
    }
}
