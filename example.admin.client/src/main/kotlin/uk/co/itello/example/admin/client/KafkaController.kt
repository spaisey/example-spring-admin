package uk.co.itello.example.admin.client

import cern.jet.random.Normal
import cern.jet.random.engine.MersenneTwister64
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.Disposable
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import reactor.core.publisher.Flux
import java.time.Duration


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@RestController
class KafkaController(
		private val kafkaTemplate: KafkaTemplate<Any, Any>,
		private val registry: MeterRegistry
) {
	private var intervalSub: Disposable? = null
	private var simulationSub: Disposable? = null

	private val r = MersenneTwister64(0)
	private val incomingRequests = Normal(0.0, 1.0, r)
	private val duration = Normal(250.0, 50.0, r)
	private val latencyForThisSecond = AtomicInteger(duration.nextInt())
	private val successFail = Random()
	companion object {
		val LOG = LoggerFactory.getLogger(KafkaController::class.java)!!
	}

	@GetMapping("msg/{topic}/{msg}")
	fun msg(@PathVariable("topic") topic: String,
			@PathVariable("msg") msg: String): String {
		return kafkaTemplate.send(topic, msg).get().toString()
	}

	@PostMapping("sim/start")
	fun simStart() {
		if (intervalSub != null) {
			return LOG.warn("Simulation already running")
		}
		intervalSub = Flux.interval(Duration.ofSeconds(1))
				.doOnEach { d -> latencyForThisSecond.set(duration.nextInt()) }
				.subscribe()

		// the potential for an "incoming request" every 10 ms
		simulationSub = Flux.interval(Duration.ofMillis(10))
				.doOnEach { d ->
					simulateCall("books", "/api/books", 0.4)
					simulateCall("people", "/api/people", 0.8)
					simulateCall("things", "/api/things", 0.4)
				}
				.subscribe()
		LOG.info("Started simulation")
	}

	@PostMapping("sim/end")
	fun simEnd() {
		intervalSub?.let {
			it.dispose()
			LOG.info("Stopping interval simulation")
			intervalSub = null
		}
		simulationSub?.let {
			it.dispose()
			LOG.info("Stopping simulation")
			simulationSub = null
		}
	}

	@KafkaListener(topics = ["test"])
	fun listener(msg: String) = LOG.info("Message: $msg")

	private fun simulateCall(client: String, endpoint: String, bias: Double) {
		if (incomingRequests.nextDouble() + bias > 0) {
			// pretend the request took some amount of time, such that the time is
			// distributed normally with a mean of 250ms
			Timer.builder("http.client.requests")
					.tag("response", if (successFail.nextGaussian() > 0.2) "200" else "400")
					.tag("uri", endpoint)
					.tag("client", client)
					.register(registry)
					.record(latencyForThisSecond.get().toLong(), TimeUnit.MILLISECONDS)
		}
	}
}
