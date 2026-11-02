package com.muneeb.quran16line.pages

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.muneeb.quran16line.data.PageStyle
import kotlin.math.cos
import kotlin.math.sin

// 4x5 row-major colour matrices, offsets in 0..255 (Android / Compose ColorMatrix convention).

private val IDENTITY = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private val INVERT = floatArrayOf(
    -1f, 0f, 0f, 0f, 255f,
    0f, -1f, 0f, 0f, 255f,
    0f, 0f, -1f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f,
)

/** Luminance-preserving hue rotation (same as CSS hue-rotate / SVG feColorMatrix). */
private fun hueRotate(degrees: Float): FloatArray {
    val rad = Math.toRadians(degrees.toDouble())
    val c = cos(rad).toFloat()
    val s = sin(rad).toFloat()
    val lr = 0.213f
    val lg = 0.715f
    val lb = 0.072f
    return floatArrayOf(
        lr + c * (1 - lr) - s * lr, lg - c * lg - s * lg, lb - c * lb + s * (1 - lb), 0f, 0f,
        lr - c * lr + s * 0.143f, lg + c * (1 - lg) + s * 0.140f, lb - c * lb - s * 0.283f, 0f, 0f,
        lr - c * lr - s * (1 - lr), lg - c * lg + s * lg, lb + c * (1 - lb) + s * lb, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}

private fun brightness(k: Float) = floatArrayOf(
    k, 0f, 0f, 0f, 0f,
    0f, k, 0f, 0f, 0f,
    0f, 0f, k, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

/** CSS sepia(amount). */
private fun sepia(amount: Float): FloatArray {
    val a = 1 - amount
    return floatArrayOf(
        0.393f + 0.607f * a, 0.769f - 0.769f * a, 0.189f - 0.189f * a, 0f, 0f,
        0.349f - 0.349f * a, 0.686f + 0.314f * a, 0.168f - 0.168f * a, 0f, 0f,
        0.272f - 0.272f * a, 0.534f - 0.534f * a, 0.131f + 0.869f * a, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}

/** Returns the matrix that applies [first] and then [second]. */
private fun then(first: FloatArray, second: FloatArray): FloatArray {
    val out = FloatArray(20)
    for (r in 0 until 4) {
        for (c in 0 until 5) {
            var v = if (c == 4) second[r * 5 + 4] else 0f
            for (k in 0 until 4) v += second[r * 5 + k] * first[k * 5 + c]
            out[r * 5 + c] = v
        }
    }
    return out
}

fun pageStyleMatrix(style: PageStyle): FloatArray = when (style) {
    PageStyle.AUTO, PageStyle.ORIGINAL -> IDENTITY
    PageStyle.INVERT -> INVERT
    PageStyle.NIGHT -> then(INVERT, hueRotate(180f))
    PageStyle.WARM -> then(brightness(0.62f), sepia(0.25f))
}

private val filters = PageStyle.entries.associateWith { style ->
    if (style == PageStyle.ORIGINAL || style == PageStyle.AUTO) null else ColorFilter.colorMatrix(ColorMatrix(pageStyleMatrix(style)))
}

fun pageColorFilter(style: PageStyle): ColorFilter? = filters[style]

/** The colour around the page, run through the same filter so it blends with the page edge. */
fun letterboxColor(bg: Color, style: PageStyle): Color {
    val m = pageStyleMatrix(style)
    val c = floatArrayOf(bg.red * 255, bg.green * 255, bg.blue * 255, 255f)
    val out = FloatArray(3) { r ->
        (m[r * 5] * c[0] + m[r * 5 + 1] * c[1] + m[r * 5 + 2] * c[2] + m[r * 5 + 3] * c[3] + m[r * 5 + 4])
            .coerceIn(0f, 255f) / 255f
    }
    return Color(out[0], out[1], out[2])
}
