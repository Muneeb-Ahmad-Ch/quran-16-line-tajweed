package com.muneeb.quran16line.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class Bookmark(val page: Int, val name: String, val createdAt: Long)

object BookmarkCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(list: List<Bookmark>): String = json.encodeToString(list)

    fun decode(s: String?): List<Bookmark> {
        if (s.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Bookmark>>(s)
        } catch (_: SerializationException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }
}
