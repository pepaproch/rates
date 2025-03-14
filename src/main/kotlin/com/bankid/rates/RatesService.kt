package com.bankid.rates

import com.bankid.rates.client.ExchangeRates
import com.bankid.rates.client.ExchangeResponse
import com.bankid.rates.client.RatesFetchService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class RatesService(val fetchService: RatesFetchService) {

    // Cache to store exchange rate comparisons
    private val exchangeRatesCache: MutableMap<String, ExchangeRates> = ConcurrentHashMap()
    private var lastUpdateDate: String? = null
    private var comparisonCache : MutableMap<String, Comparison> = ConcurrentHashMap()

    fun getPairs(): Collection<String> {
        checkData()
        return comparisonCache["FRANKFURTER"]?.comparison?.keys ?: emptyList()
    }




    fun getComparison(): ExchangeResponse? {
        return fetchService.fetchComparsionRates()
    }

    fun checkData() {
        if (lastUpdateDate != LocalDate.now().toString() || comparisonCache.isEmpty()) {
            prepareComparison()
            lastUpdateDate = LocalDate.now().toString()
        }
    }

    fun getComparisonRate(provider: String, pair: String): PairCompare? {
        checkData()

        comparisonCache[provider]?.comparison?.get(pair)?.let { rate ->
           val rb =  exchangeRatesCache["CNB"]?.tabulka?.radek?.find { it.code == pair.substring(3) }?.rateString ?: BigDecimal.ZERO
             return PairCompare(pair, baseRate = rb, rate = rate)
        }
        return null
    }

    fun prepareComparison() {
        val exchangeRates = fetchService.fetchExchangeRates()
        val comparisonRates = fetchService.fetchComparsionRates()

        // map to map

        val comparisonMap = exchangeRates?.tabulka?.radek?.mapNotNull { r ->
            val pair = "CZK" + r.code
            val comparisonRate = comparisonRates?.rates?.get(r.code)
            comparisonRate?.let { rate ->
                pair to BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP).multiply(BigDecimal(r.amount))
            }
        }?.toMap() ?: emptyMap()
        exchangeRates?.let { exchangeRatesCache["CNB"] = it }
        comparisonCache["FRANKFURTER"] = Comparison(LocalDate.now(),comparisonMap)



    }
    data class PairCompare(val pair: String, val baseRate: Number, val rate: BigDecimal)
    data class Comparison(val updated: LocalDate, val comparison: Map<String, BigDecimal>)



}