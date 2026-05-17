package exam.hughwu.retrofit.twse.model.exchangereport.response


import com.google.gson.annotations.SerializedName

data class StockPriceAvg(
    @SerializedName("ClosingPrice")
    val closingPrice: String?,
    @SerializedName("Code")
    val code: String?,
    @SerializedName("Date")
    val date: String?,
    @SerializedName("MonthlyAveragePrice")
    val monthlyAveragePrice: String?,
    @SerializedName("Name")
    val name: String?
)