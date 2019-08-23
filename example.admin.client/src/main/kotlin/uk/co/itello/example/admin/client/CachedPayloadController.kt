package uk.co.itello.example.admin.client

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.http.ResponseEntity.notFound as httpNotFound
import org.springframework.http.ResponseEntity.ok as httpOk

/**
 * Simple caching endpoint with also demonstrates a @Schedule. Both caches and schedules are picked up by the admin
 * server.
 */
@RestController
@RequestMapping("cache")
class CachedPayloadController {
    private val store = ConcurrentHashMap<String, String>()

    companion object {
        private const val CACHE_NAME = "payload"
        private const val ID_PATH_VARIABLE = "id"
        private const val ID_PATH_VARIABLE_MAPPING = "{$ID_PATH_VARIABLE}"
        private val LOG = LoggerFactory.getLogger(CachedPayloadController::class.java)
    }

    /**
     * Create a record
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody payload: String): ResponseEntity<Unit> {
        val id = UUID.randomUUID().toString()
        store[id] = payload
        LOG.info("Created: $id")
        return ResponseEntity.created(URI("/cache/$id")).build()
    }

    /**
     * Retrieve a record
     */
    @GetMapping(ID_PATH_VARIABLE_MAPPING, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Cacheable(CACHE_NAME)
    fun retrieve(@PathVariable(ID_PATH_VARIABLE) id: String): ResponseEntity<String> {
        LOG.info("Retrieving record [$id] from store")
        return if (store[id] == null) httpNotFound().build() else httpOk(store[id]!!)
    }

    /**
     * Update a record
     */
    @PutMapping(value = [ID_PATH_VARIABLE_MAPPING], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @CachePut(cacheNames = [CACHE_NAME], key = "#id")
    fun update(@PathVariable(ID_PATH_VARIABLE) id: String, @RequestBody payload: String): ResponseEntity<String> =
            if (store[id] == null) httpNotFound().build() else httpOk(store.replace(id, payload)!!)

    /**
     * Remove a record
     */
    @DeleteMapping(ID_PATH_VARIABLE_MAPPING)
    @CacheEvict(CACHE_NAME)
    fun remove(@PathVariable(ID_PATH_VARIABLE) id: String): ResponseEntity<String> =
            if (store[id] == null) httpNotFound().build() else httpOk(store.remove(id)!!)

    /**
     * Clear all records
     */
    @DeleteMapping
    @Scheduled(fixedDelay = 600000)
    @CacheEvict(CACHE_NAME, allEntries=true)
    fun clear(): Unit = store.clear()
}


