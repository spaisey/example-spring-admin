package uk.co.itello.example.admin.client

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.kafka.KafkaConsumerMetrics
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MeterController(private val mr: MeterRegistry) {

    @GetMapping("meter")
    fun meter(): String {
        val builder = StringBuilder()
        mr.forEachMeter {
            builder.append(it.id).append('\n')
        }

        return builder.toString()
    }

    @GetMapping("meter/search")
    fun search(@RequestParam("q") query: String): String {
        val builder = StringBuilder()
        mr.find(query).summaries().forEach {
            builder.append(it).append('\n')
        }
        return builder.toString()
    }
}
