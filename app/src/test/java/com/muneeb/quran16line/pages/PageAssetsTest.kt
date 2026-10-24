package com.muneeb.quran16line.pages

import com.muneeb.quran16line.data.MushafVersion
import org.junit.Assert.assertEquals
import org.junit.Test

class PageAssetsTest {

    @Test
    fun assetPathFormat() {
        assertEquals("pages/color/0024.webp", PageAssets.page(MushafVersion.COLOR, 24))
        assertEquals("pages/plain/0549.webp", PageAssets.page(MushafVersion.PLAIN, 549))
        assertEquals("extras/03.webp", PageAssets.extra(3))
    }

    @Test
    fun manifestParses() {
        val m = PageManifest.parse(
            """{"plain":{"width":1200,"height":1763,"bg":"#ffffff"},
               "color":{"width":1200,"height":1838,"bg":"#fefefe"},
               "extras":{"count":11}}"""
        )
        assertEquals(1200f / 1763f, m.of(MushafVersion.PLAIN).aspect, 1e-6f)
        assertEquals(1200, m.of(MushafVersion.COLOR).width)
        assertEquals(0xFFFEFEFE.toInt(), m.of(MushafVersion.COLOR).bgArgb)
        assertEquals(11, m.extras.count)
    }
}
