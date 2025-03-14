package com.bankid.rates.client

import com.fasterxml.jackson.annotation.JsonCreator

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.http.codec.xml.Jaxb2XmlDecoder


import org.springframework.http.codec.xml.Jaxb2XmlEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal


@Service
class RatesFetchService {

    private val webClient = WebClient.builder()
        .baseUrl("https://www.cnb.cz/cs/financni_trhy/devizovy_trh/kurzy_devizoveho_trhu/denni_kurz.xml") // Upravte na skutečné API
        .defaultHeader("Accept", "application/xml")
        .build()

    private val frankfurterApi = WebClient.builder()
        .baseUrl("https://api.frankfurter.dev/v1/latest?base=CZK") // Upravte na skutečné API
        .defaultHeader("Accept", "application/json")
        .build()


    fun fetchExchangeRates(): ExchangeRates? {
        val xml = webClient.get() // API endpoint
            .retrieve()
            .bodyToMono(String::class.java)
            .block() // 🚀
            // not able to parse the xml response ?
        val xmlMapper = XmlMapper() // Jackson XML mapper
        val exchangeRates = xmlMapper.readValue(xml, ExchangeRates::class.java)
        return exchangeRates
    }

    fun fetchComparsionRates(): ExchangeResponse? {
      return frankfurterApi.get() // API endpoint
            .retrieve()
            .bodyToMono(ExchangeResponse::class.java)
            .block() // 🚀


    }








}

data class ExchangeResponse(
    val amount: Int,
    val base: String,
    val date: String,
    val rates: Map<String, BigDecimal>
)


@JacksonXmlRootElement(localName = "kurzy")
data class ExchangeRates(
    @JacksonXmlProperty(localName = "banka", isAttribute = true) val bank: String, // Atribut "banka"
    @JacksonXmlProperty(localName = "datum", isAttribute = true) val date: String, // Atribut "datum"
    @JacksonXmlProperty(localName = "poradi", isAttribute = true) val order: Int, // Atribut "poradi"
    @JacksonXmlProperty(localName = "tabulka") val tabulka: ExchangeTable // Vnořený element "tabulka"
)

// Element "tabulka" obsahující seznam měn
data class ExchangeTable @JsonCreator constructor(
    @JacksonXmlProperty(localName = "typ", isAttribute = true) val type: String, // Atribut "typ"
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "radek") val radek: List<Currency> // Seznam měn (radek)
)



// Element "radek" pro jednotlivé měny
data class Currency @JsonCreator constructor(
    @JacksonXmlProperty(localName = "kod", isAttribute = true) val code: String, // Atribut "kod"
    @JacksonXmlProperty(localName = "mena", isAttribute = true) val currency: String, // Atribut "mena"
    @JacksonXmlProperty(localName = "mnozstvi", isAttribute = true) val amount: Int, // Atribut "mnozstvi"
    @JacksonXmlProperty(localName = "kurz", isAttribute = true) @JsonDeserialize(using = CurrencyDeserializer::class) val rateString: BigDecimal, // Atribut "kurz"
    @JacksonXmlProperty(localName = "zeme", isAttribute = true) val country: String  // Atribut "zeme"

)
class CurrencyDeserializer : JsonDeserializer<BigDecimal>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
        return BigDecimal(p.text.replace(",", ".")) // Převod desetinné čárky na tečku
    }
}