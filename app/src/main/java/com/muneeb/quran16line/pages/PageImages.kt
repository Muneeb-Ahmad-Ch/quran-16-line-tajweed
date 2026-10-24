package com.muneeb.quran16line.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.muneeb.quran16line.data.MushafVersion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PageAssets {
    fun page(version: MushafVersion, page: Int) = "pages/${version.assetDir}/%04d.webp".format(page)
    fun extra(i: Int) = "extras/%02d.webp".format(i)
    const val MANIFEST = "pages/manifest.json"
}

@Serializable
data class VersionInfo(val width: Int, val height: Int, val bg: String) {
    val aspect: Float get() = width.toFloat() / height
    val bgArgb: Int get() = (0xFF000000 or bg.removePrefix("#").toLong(16)).toInt()
}

@Serializable
data class ExtrasInfo(val count: Int)

@Serializable
data class PageManifest(val plain: VersionInfo, val color: VersionInfo, val extras: ExtrasInfo) {
    fun of(version: MushafVersion) = if (version == MushafVersion.PLAIN) plain else color

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun parse(text: String): PageManifest = json.decodeFromString(text)
    }
}

/**
 * Decodes page images from assets at the width they are drawn, off the main thread,
 * with an in-memory cache and de-duplicated in-flight decodes for prefetching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PageImages(context: Context) {
    private val assets = context.applicationContext.assets
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(3))
    private val inFlight = HashMap<String, Deferred<Bitmap>>()

    val manifest: PageManifest = assets.open(PageAssets.MANIFEST).use { PageManifest.parse(it.reader().readText()) }

    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 4).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    /** Returns a decoded bitmap if it's already cached (no waiting). */
    fun cached(path: String, targetWidth: Int): ImageBitmap? = cache.get(key(path, targetWidth))?.asImageBitmap()

    suspend fun load(path: String, targetWidth: Int): ImageBitmap = request(path, targetWidth).await().asImageBitmap()

    fun prefetch(paths: List<String>, targetWidth: Int) {
        paths.forEach { request(it, targetWidth) }
    }

    private fun key(path: String, targetWidth: Int) = "$path@$targetWidth"

    private fun request(path: String, targetWidth: Int): Deferred<Bitmap> = synchronized(inFlight) {
        val k = key(path, targetWidth)
        cache.get(k)?.let { return CompletableDeferred(it) }
        inFlight[k]?.let { return it }
        scope.async {
            try {
                decode(path, targetWidth).also { cache.put(k, it) }
            } finally {
                synchronized(inFlight) { inFlight.remove(k) }
            }
        }.also { inFlight[k] = it }
    }

    private fun decode(path: String, targetWidth: Int): Bitmap {
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (targetWidth in 1 until bounds.outWidth) {
            // Scale while decoding so we never hold a bigger bitmap than the screen needs.
            opts.inScaled = true
            opts.inDensity = bounds.outWidth
            opts.inTargetDensity = targetWidth
        }
        return assets.open(path).use { requireNotNull(BitmapFactory.decodeStream(it, null, opts)) { "decode failed: $path" } }
    }
}
