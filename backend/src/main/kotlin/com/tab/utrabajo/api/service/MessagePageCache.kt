package com.tab.utrabajo.api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class MessagePageCacheStats(
    val enabled: Boolean,
    val ttlMilliseconds: Long,
    val maximumEntries: Int,
    val currentEntries: Int,
    val hits: Long,
    val misses: Long,
    val puts: Long,
    val invalidations: Long,
)

@Component
class MessagePageCache(
    @Value("\${app.message-cache.enabled:false}") private val enabled: Boolean,
    @Value("\${app.message-cache.ttl-ms:1000}") private val ttlMilliseconds: Long,
    @Value("\${app.message-cache.maximum-entries:1000}") private val maximumEntries: Int,
) {
    private data class Key(val chatId: UUID, val limit: Int, val offset: Int)
    private data class Entry(val messages: List<Map<String, Any?>>, val expiresAtNanos: Long)

    private val entries = ConcurrentHashMap<Key, Entry>()
    private val hits = AtomicLong()
    private val misses = AtomicLong()
    private val puts = AtomicLong()
    private val invalidations = AtomicLong()

    init {
        require(ttlMilliseconds > 0) { "El TTL de mensajes debe ser positivo" }
        require(maximumEntries > 0) { "La capacidad de caché debe ser positiva" }
    }

    fun getOrLoad(
        chatId: UUID,
        limit: Int,
        offset: Int,
        loader: () -> List<Map<String, Any?>>,
    ): List<Map<String, Any?>> {
        if (!enabled || offset != 0) return loader()

        val key = Key(chatId, limit, offset)
        val now = System.nanoTime()
        val entry = entries.compute(key) { _, current ->
            if (current != null && current.expiresAtNanos > now) {
                hits.incrementAndGet()
                current
            } else {
                misses.incrementAndGet()
                puts.incrementAndGet()
                Entry(loader(), now + ttlMilliseconds * 1_000_000)
            }
        } ?: error("No fue posible resolver la página de mensajes")
        evictExpiredOrExcess(key, now)
        return entry.messages
    }

    fun invalidateChat(chatId: UUID) {
        if (!enabled) return
        val removed = entries.keys.removeIf { it.chatId == chatId }
        if (removed) invalidations.incrementAndGet()
    }

    fun stats(): MessagePageCacheStats = MessagePageCacheStats(
        enabled = enabled,
        ttlMilliseconds = ttlMilliseconds,
        maximumEntries = maximumEntries,
        currentEntries = entries.size,
        hits = hits.get(),
        misses = misses.get(),
        puts = puts.get(),
        invalidations = invalidations.get(),
    )

    fun reset() {
        entries.clear()
        hits.set(0)
        misses.set(0)
        puts.set(0)
        invalidations.set(0)
    }

    private fun evictExpiredOrExcess(keep: Key, now: Long) {
        entries.entries.removeIf { (key, value) -> key != keep && value.expiresAtNanos <= now }
        while (entries.size > maximumEntries) {
            val candidate = entries.keys.firstOrNull { it != keep } ?: break
            entries.remove(candidate)
        }
    }
}
