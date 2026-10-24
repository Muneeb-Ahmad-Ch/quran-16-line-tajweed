package com.muneeb.quran16line.ui.reader

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.muneeb.quran16line.pages.PageImages
import kotlinx.coroutines.CancellationException
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * One page image: decoded off the main thread (or taken straight from cache), drawn with a
 * few dp of side gap, pinch / double-tap zoomable. Until the bitmap is ready nothing is drawn,
 * so the caller's background shows instead of a spinner.
 */
@Composable
fun ZoomablePage(path: String, images: PageImages, targetWidth: Int, colorFilter: ColorFilter?) {
    var bitmap by remember(path, targetWidth) { mutableStateOf<ImageBitmap?>(images.cached(path, targetWidth)) }
    if (bitmap == null) {
        LaunchedEffect(path, targetWidth) {
            bitmap = try {
                images.load(path, targetWidth)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                null // missing asset or out of memory: keep showing the background instead of crashing
            }
        }
    }
    val zoom = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f))
    val b = bitmap ?: return
    LaunchedEffect(b) {
        zoom.setContentLocation(
            ZoomableContentLocation.scaledInsideAndCenterAligned(Size(b.width.toFloat(), b.height.toFloat()))
        )
    }
    Image(
        bitmap = b,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().zoomable(zoom).padding(horizontal = 3.dp),
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter,
    )
}

/** Hides status and navigation bars while shown; a swipe from the edge reveals them briefly. */
@Composable
fun ImmersiveMode() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
}

@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}
