package com.tab.utrabajo.api.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.util.UUID

class MessagePageCacheTests {
    @Test
    fun `enabled cache reuses latest page and invalidates by chat`() {
        val cache = MessagePageCache(enabled = true, ttlMilliseconds = 60_000, maximumEntries = 10)
        val chatId = UUID.randomUUID()
        var loads = 0
        val loader = {
            loads += 1
            listOf(mapOf("message" to "version-$loads"))
        }

        val first = cache.getOrLoad(chatId, 50, 0, loader)
        val second = cache.getOrLoad(chatId, 50, 0, loader)

        assertEquals(first, second)
        assertEquals(1, loads)
        assertEquals(1, cache.stats().hits)
        assertEquals(1, cache.stats().misses)

        cache.invalidateChat(chatId)
        val third = cache.getOrLoad(chatId, 50, 0, loader)

        assertEquals("version-2", third.single()["message"])
        assertEquals(2, loads)
        assertEquals(1, cache.stats().invalidations)
    }

    @Test
    fun `disabled cache and deep pages always delegate`() {
        val disabled = MessagePageCache(enabled = false, ttlMilliseconds = 1_000, maximumEntries = 10)
        val enabled = MessagePageCache(enabled = true, ttlMilliseconds = 1_000, maximumEntries = 10)
        val chatId = UUID.randomUUID()
        var disabledLoads = 0
        var deepLoads = 0

        repeat(2) { disabled.getOrLoad(chatId, 50, 0) { disabledLoads += 1; emptyList() } }
        repeat(2) { enabled.getOrLoad(chatId, 50, 50) { deepLoads += 1; emptyList() } }

        assertEquals(2, disabledLoads)
        assertEquals(2, deepLoads)
        assertFalse(disabled.stats().enabled)
        assertTrue(enabled.stats().enabled)
        assertEquals(0, enabled.stats().currentEntries)
    }
}
