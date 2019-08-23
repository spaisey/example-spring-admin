package uk.co.itello.example.admin.client

import org.apache.kafka.clients.admin.AdminClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.boot.actuate.health.Health
import org.apache.kafka.clients.admin.DescribeClusterOptions
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaAdmin
import java.util.concurrent.ExecutionException
import org.apache.kafka.clients.admin.DescribeClusterResult


@SpringBootApplication
//@EnableWebMvc
@EnableKafka
class Application {
    @Bean
    fun kafkaAdminClient(admin: KafkaAdmin): AdminClient {
        return AdminClient.create(admin.config)
    }

    @Bean
    fun kafkaHealthIndicator(kafkaAdminClient: AdminClient): HealthIndicator {
        val describeCluster = kafkaAdminClient.describeCluster()

        return HealthIndicator {
            try {
                val clusterId = describeCluster.clusterId().get()
                val nodeCount = describeCluster.nodes().get().size
                Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build()
            } catch (e: Exception) {
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
