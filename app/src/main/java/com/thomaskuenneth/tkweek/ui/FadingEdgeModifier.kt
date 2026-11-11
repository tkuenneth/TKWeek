/*
 * FadingEdgeModifier.kt
 *
 * Copyright 2026 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.ui

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.thomaskuenneth.tkweek.R
import kotlin.math.max

@Composable
fun Modifier.fadingEdge(scrollableState: ScrollableState): Modifier = fadingEdge(
    fadeTop = scrollableState.canScrollBackward,
    fadeBottom = scrollableState.canScrollForward
)

@Composable
fun Modifier.fadingEdge(fadeTop: Boolean = true, fadeBottom: Boolean = true): Modifier {
    val density = LocalDensity.current
    val contentPadding = with(density) { dimensionResource(R.dimen.content_padding).toPx() }
    val statusBarBottom = WindowInsets.statusBars.getTop(density).toFloat()
    val bottomFadeHeight = WindowInsets.navigationBars.getBottom(density).toFloat()
    var positionInWindow by remember { mutableFloatStateOf(0f) }
    return this
        .onPlaced { positionInWindow = it.positionInWindow().y }
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()

            val topFadeHeight = max(statusBarBottom - positionInWindow, contentPadding)
            val height = size.height
            if (height > topFadeHeight + bottomFadeHeight) {
                val topFadeStop = topFadeHeight / height
                val bottomFadeStop = (height - bottomFadeHeight) / height

                val topAlpha = if (fadeTop) Color.Transparent else Color.Black
                val bottomAlpha = if (fadeBottom) Color.Transparent else Color.Black

                drawRect(
                    brush = Brush.verticalGradient(
                        0f to topAlpha,
                        topFadeStop to Color.Black,
                        bottomFadeStop to Color.Black,
                        1f to bottomAlpha
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
}
