package com.muneeb.quran16line.pages

import androidx.compose.ui.graphics.Color
import com.muneeb.quran16line.data.PageStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PageStylesTest {

    /** Applies a 4x5 colour matrix (offsets in 0..255) to an RGB colour given in 0..1. */
    private fun apply(m: FloatArray, r: Float, g: Float, b: Float): FloatArray {
        val c = floatArrayOf(r * 255, g * 255, b * 255, 255f)
        return FloatArray(3) { row ->
            val v = m[row * 5] * c[0] + m[row * 5 + 1] * c[1] + m[row * 5 + 2] * c[2] + m[row * 5 + 3] * c[3] + m[row * 5 + 4]
            v.coerceIn(0f, 255f) / 255f
        }
    }

    private fun lum(c: FloatArray) = 0.2126f * c[0] + 0.7152f * c[1] + 0.0722f * c[2]

    @Test
    fun invertWhiteIsBlack() {
        val out = apply(pageStyleMatrix(PageStyle.INVERT), 1f, 1f, 1f)
        out.forEach { assertEquals(0f, it, 0.001f) }
    }

    @Test
    fun nightWhiteIsBlack() {
        val out = apply(pageStyleMatrix(PageStyle.NIGHT), 1f, 1f, 1f)
        out.forEach { assertEquals(0f, it, 0.02f) }
    }

    @Test
    fun nightBlackIsWhite() {
        val out = apply(pageStyleMatrix(PageStyle.NIGHT), 0f, 0f, 0f)
        out.forEach { assertEquals(1f, it, 0.02f) }
    }

    @Test
    fun nightKeepsHueRoughly() {
        val blue = apply(pageStyleMatrix(PageStyle.NIGHT), 0f, 0f, 1f)
        assertTrue(blue.contentToString(), blue[2] > blue[0] && blue[2] > blue[1])
        val red = apply(pageStyleMatrix(PageStyle.NIGHT), 1f, 0f, 0f)
        assertTrue(red.contentToString(), red[0] > red[1] && red[0] > red[2])
    }

    @Test
    fun warmDimsWhite() {
        val out = apply(pageStyleMatrix(PageStyle.WARM), 1f, 1f, 1f)
        assertTrue(out.contentToString(), lum(out) in 0.5f..0.75f)
        assertTrue("warm means red >= blue", out[0] >= out[2])
    }

    @Test
    fun originalHasNoFilter() {
        assertNull(pageColorFilter(PageStyle.ORIGINAL))
    }

    @Test
    fun letterboxFollowsStyle() {
        assertEquals(Color.White, letterboxColor(Color.White, PageStyle.ORIGINAL))
        val night = letterboxColor(Color.White, PageStyle.NIGHT)
        assertTrue(night.red < 0.03f && night.green < 0.03f && night.blue < 0.03f)
    }
}
