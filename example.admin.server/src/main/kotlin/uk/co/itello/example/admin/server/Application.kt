package uk.co.itello.example.admin.server

import de.codecentric.boot.admin.server.config.EnableAdminServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAdminServer
@EnableAsync
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
