package uk.co.itello.example.admin.client

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {

	@GetMapping("ping")
	fun ping(): String = "hello from client"
}
