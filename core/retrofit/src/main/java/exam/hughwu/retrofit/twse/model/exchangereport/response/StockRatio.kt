package exam.hughwu.retrofit.twse.model.exchangereport.response

import com.google.gson.annotations.SerializedName

data class StockRatio(
    @SerializedName("Code")
    val code: String?,
    @SerializedName("Date")
    val date: String?,
    @SerializedName("DividendYield")
    val dividendYield: String?,
    @SerializedName("Name")
    val name: String?,
    @SerializedName("PBratio")
    val pBratio: String?,
    @SerializedName("PEratio")
    val pEratio: String?
)