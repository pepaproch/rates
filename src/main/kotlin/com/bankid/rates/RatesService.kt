package com.bankid.rates

import com.bankid.rates.client.ExchangeRates
import com.bankid.rates.client.ExchangeResponse
import com.bankid.rates.client.RatesFetchService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap


@Service
class RatesService(val fetchService: RatesFetchService) {
    companion object {
        private const val BASE_PROVIDER = "CNB"
        private const val COMPARE_PROVIDER = "FRANKFURTER"
    }
    private val logger: Logger = LoggerFactory.getLogger(RatesService::class.java)
    private val exchangeRatesCache: MutableMap<String, ExchangeRates> = ConcurrentHashMap()
    private var lastUpdateDate: LocalDate? = null
    private var comparisonCache: MutableMap<String, Comparison> = ConcurrentHashMap()

    fun getPairs(): Collection<String> {
        checkData()
        return comparisonCache[COMPARE_PROVIDER]?.comparison?.keys ?: emptyList()
    }



    fun getComparisonRate(provider: String, pair: String): PairCompare? {
        checkData()
        return comparisonCache[provider]?.comparison?.get(pair)?.let { rate ->
            val baseRate = getBaseRate(pair)
            PairCompare(provider,pair, baseRate, rate , exchangeRatesCache[BASE_PROVIDER]?.date ?: LocalDate.now(), comparisonCache[provider]?.updated ?: LocalDate.now())
        }
    }

    private fun getBaseRate(pair: String): BigDecimal {
        return exchangeRatesCache[BASE_PROVIDER]?.tabulka?.radek?.find { it.code == pair.substring(3) }?.rateString ?: BigDecimal.ZERO
    }

    private fun checkData() {
        if (isDataOutdated()) {
            prepareComparison()
            logger.info("Cache is outdated. Refreshing data.")
            lastUpdateDate = LocalDate.now()
        }
    }

    private fun isDataOutdated(): Boolean {
        return lastUpdateDate?.isBefore(LocalDate.now()) == true || comparisonCache.isEmpty() || exchangeRatesCache.isEmpty()
    }

    private fun prepareComparison() {
        val exchangeRates = fetchService.fetchExchangeRates()
        val comparisonRates = fetchService.fetchComparsionRates(exchangeRates?.date ?: LocalDate.now())
        updateCaches(exchangeRates, comparisonRates)
    }

    private fun updateCaches(exchangeRates: ExchangeRates?, comparisonRates: ExchangeResponse?) {
        val comparisonMap = createComparisonMap(exchangeRates, comparisonRates)
        exchangeRates?.let { exchangeRatesCache[BASE_PROVIDER] = it }
        comparisonCache[COMPARE_PROVIDER] = Comparison(exchangeRates?.date ?: LocalDate.now(), comparisonMap)
    }

    private fun createComparisonMap(exchangeRates: ExchangeRates?, comparisonRates: ExchangeResponse?): Map<String, BigDecimal> {
        return exchangeRates?.tabulka?.radek?.mapNotNull { r ->
            val pair = "CZK" + r.code
            val comparisonRate = comparisonRates?.rates?.get(r.code)
            comparisonRate?.let { rate ->
                pair to BigDecimal.ONE.divide(rate, 4, RoundingMode.HALF_UP).multiply(BigDecimal(r.amount))
            }
        }?.toMap() ?: emptyMap()
    }

    data class PairCompare(val source:String , val pair: String, val cnbRate: Number, val rate: BigDecimal , val cnbDate: LocalDate, val rateDate: LocalDate)
    data class Comparison(val updated: LocalDate, val comparison: Map<String, BigDecimal>)
}