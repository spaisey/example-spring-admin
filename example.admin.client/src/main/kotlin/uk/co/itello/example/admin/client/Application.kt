package uk.co.itello.example.admin.client

import org.apache.kafka.clients.admin.AdminClient
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.TimeUnit.MILLISECONDS


@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
class Application {
    companion object {
        private val LOG = LoggerFactory.getLogger(Application::class.java)!!
    }
    @Bean
    fun kafkaAdminClient(admin: KafkaAdmin): AdminClient {
        return AdminClient.create(admin.config)
    }

    @Bean
    fun kafkaHealthIndicator(kafkaAdminClient: AdminClient): HealthIndicator {
        return HealthIndicator {
            try {
                val describeCluster = kafkaAdminClient.describeCluster()
                val clusterId = describeCluster.clusterId().get(2000, MILLISECONDS)
                val nodeCount = describeCluster.nodes().get(2000, MILLISECONDS).size
                LOG.debug("Health check requested - we're GOOD")
                Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build()
            } catch (e: Exception) {
                LOG.warn("Health check requested - we're BAD", e)
                Health.down()
                        .withException(e)
                        .build()
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
