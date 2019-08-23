package uk.co.itello.example.admin.client

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@RestController
class KafkaController(private val kafkaTemplate: KafkaTemplate<Any, Any>) {

	companion object {
		val LOG = LoggerFactory.getLogger(KafkaController::class.java)!!
	}

	@GetMapping("msg/{topic}/{msg}")
	fun msg(@PathVariable("topic") topic: String,
			@PathVariable("msg") msg: String): String {
		return kafkaTemplate.send(topic, msg).get().toString()
	}

	@KafkaListener(topics = ["test"])
	fun listener(msg: String) = LOG.info("Message: $msg")
}
