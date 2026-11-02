package com.muneeb.quran16line.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarkCodecTest {
    @Test
    fun roundTripsAwkwardNames() {
        val list = listOf(
            Bookmark(24, "Sayaqool – Al-Baqarah – Pg 24", 5),
            Bookmark(30, "quote \" apostrophe ' slash \\ newline\nemoji 😀 | ; ,", 4),
            Bookmark(40, "", 3),
        )
        assertEquals(list, BookmarkCodec.decode(BookmarkCodec.encode(list)))
    }

    @Test
    fun decodeGarbageReturnsEmpty() {
        assertEquals(emptyList<Bookmark>(), BookmarkCodec.decode("not json {"))
        assertEquals(emptyList<Bookmark>(), BookmarkCodec.decode(null))
        assertEquals(emptyList<Bookmark>(), BookmarkCodec.decode(""))
    }
}
