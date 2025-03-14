package com.bankid.rates.rest
import com.bankid.rates.RatesService
import com.bankid.rates.client.ExchangeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.math.BigDecimal

@Configuration
@RestController
@RequestMapping("/api")
class RatesApi(private val ratesService: RatesService) : WebMvcConfigurer  {



    @Operation(summary = "Get supPorted pairs")
    @GetMapping("/pairs" ,produces = [MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE])
    fun service() : Collection<String>
    {
        return ratesService.getPairs();
    }



    @Operation(summary = "Get diff")
    @GetMapping("/FRANKFURTER/{pair}" ,produces = [MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE])
    fun diff(
        @PathVariable pair: String): RatesService.PairCompare? {
        return ratesService.getComparisonRate("FRANKFURTER" , pair);
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/doc").setViewName("forward:/index.html")
    }




    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BankId REST API")
                    .version("1.0")
                    .description("Dokumentace k REST API")
            )
    }

    data class Pairs(val pairs: List<String>)
}