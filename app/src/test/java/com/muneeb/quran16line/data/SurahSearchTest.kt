package com.muneeb.quran16line.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SurahSearchTest {

    private fun top(q: String) = SurahSearch.search(q).first().number

    @Test
    fun blankQueryListsAllInOrder() {
        assertEquals((1..114).toList(), SurahSearch.search("  ").map { it.number })
    }

    @Test
    fun exactAndPrefixedNames() {
        assertEquals(67, top("Al-Mulk"))
        assertEquals(67, top("al mulk"))
        assertEquals(67, top("mulk"))
        assertEquals(18, top("Kahf"))
        assertEquals(1, top("fatiha"))
    }

    @Test
    fun number() {
        assertEquals(36, top("36"))
        assertEquals(114, top("114"))
    }

    @Test
    fun commonSpellings() {
        assertEquals(36, top("yaseen"))
        assertEquals(55, top("rehman"))
        assertEquals(18, top("kahaf"))
        assertEquals(2, top("baqra"))
        assertEquals(3, top("imran"))
        assertEquals(9, top("tauba"))
        assertEquals(74, top("mudassir"))
        assertEquals(73, top("muzamil"))
        assertEquals(108, top("kausar"))
        assertEquals(114, top("naas"))
        assertEquals(56, top("waqia"))
    }

    @Test
    fun otherNames() {
        assertEquals(76, top("insan"))
        assertEquals(41, top("fussilat"))
        assertEquals(40, top("ghafir"))
        assertEquals(17, top("isra"))
        assertEquals(111, top("masad"))
    }

    @Test
    fun typos() {
        assertEquals(19, top("maryum"))
        assertEquals(12, top("yusf"))
        assertEquals(112, top("ikhlaas"))
        assertEquals(55, top("rahmaan"))
    }

    @Test
    fun surahWordIsIgnored() {
        assertEquals(36, top("surah yasin"))
        assertEquals(67, top("sura mulk"))
    }

    @Test
    fun arabic() {
        assertEquals(67, top("الملك"))
        assertEquals(67, top("ملك"))
        assertEquals(36, top("يس"))
        assertEquals(3, top("ال عمران"))
    }

    @Test
    fun alwaysSomeResultForNonsense() {
        assertTrue(SurahSearch.search("xqzv").isNotEmpty())
    }

    @Test
    fun partialTyping() {
        assertEquals(19, top("mary"))
        assertEquals(2, top("baq"))
    }
}
