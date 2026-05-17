package exam.hughwu.retrofit.twse.model.exchangereport.response


import com.google.gson.annotations.SerializedName

data class StockDailyTrading(
    @SerializedName("Change")
    val change: String?,
    @SerializedName("ClosingPrice")
    val closingPrice: String?,
    @SerializedName("Code")
    val code: String?,
    @SerializedName("Date")
    val date: String?,
    @SerializedName("HighestPrice")
    val highestPrice: String?,
    @SerializedName("LowestPrice")
    val lowestPrice: String?,
    @SerializedName("Name")
    val name: String?,
    @SerializedName("OpeningPrice")
    val openingPrice: String?,
    @SerializedName("TradeValue")
    val tradeValue: String?,
    @SerializedName("TradeVolume")
    val tradeVolume: String?,
    @SerializedName("Transaction")
    val transaction: String?
)