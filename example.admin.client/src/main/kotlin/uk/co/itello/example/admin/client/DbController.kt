package uk.co.itello.example.admin.client

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("db")
class DbController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping("check")
    fun check(): String {
        jdbcTemplate.execute("select 1")
        return "ACTIVE"
    }
}
