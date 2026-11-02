package com.muneeb.quran16line.pages

import org.junit.Assert.assertEquals
import org.junit.Test

class PageAssetsTest {

    @Test
    fun assetPathFormat() {
        assertEquals("pages/0024.webp", PageAssets.page(24))
        assertEquals("pages/0549.webp", PageAssets.page(549))
        assertEquals("extras/03.webp", PageAssets.extra(3))
    }

    @Test
    fun manifestParses() {
        val m = PageManifest.parse(
            """{"page":{"width":1200,"height":1833,"bg":"#fefefe"},"extras":{"count":11}}"""
        )
        assertEquals(1200f / 1833f, m.page.aspect, 1e-6f)
        assertEquals(1200, m.page.width)
        assertEquals(0xFFFEFEFE.toInt(), m.page.bgArgb)
        assertEquals(11, m.extras.count)
    }
}
