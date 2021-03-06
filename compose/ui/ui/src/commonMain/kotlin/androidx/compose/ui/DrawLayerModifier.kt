/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.DefaultCameraDistance
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * A [Modifier.Element] that makes content draw into a draw layer. The draw layer can be
 * invalidated separately from parents. A [drawLayer] should be used when the content
 * updates independently from anything above it to minimize the invalidated content.
 *
 * [drawLayer] can also be used to apply effects to content, such as scaling ([scaleX], [scaleY]),
 * rotation ([rotationX], [rotationY], [rotationZ]), opacity ([alpha]), shadow
 * ([shadowElevation], [shape]), and clipping ([clip], [shape]).
 *
 * If the layer parameters are backed by a [androidx.compose.runtime.State] or an animated value
 * prefer an overload with a lambda block on [GraphicsLayerScope] as reading a state inside the block
 * will only cause the layer properties update without triggering recomposition and relayout.
 *
 * @sample androidx.compose.ui.samples.ChangeOpacity
 *
 * @param scaleX see [GraphicsLayerScope.scaleX]
 * @param scaleY see [GraphicsLayerScope.scaleY]
 * @param alpha see [GraphicsLayerScope.alpha]
 * @param translationX see [GraphicsLayerScope.translationX]
 * @param translationY see [GraphicsLayerScope.translationY]
 * @param shadowElevation see [GraphicsLayerScope.shadowElevation]
 * @param rotationX see [GraphicsLayerScope.rotationX]
 * @param rotationY see [GraphicsLayerScope.rotationY]
 * @param rotationZ see [GraphicsLayerScope.rotationZ]
 * @param cameraDistance see [GraphicsLayerScope.cameraDistance]
 * @param transformOrigin see [GraphicsLayerScope.transformOrigin]
 * @param shape see [GraphicsLayerScope.shape]
 * @param clip see [GraphicsLayerScope.clip]
 */
@Stable
fun Modifier.drawLayer(
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    alpha: Float = 1f,
    translationX: Float = 0f,
    translationY: Float = 0f,
    shadowElevation: Float = 0f,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    cameraDistance: Float = DefaultCameraDistance,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    shape: Shape = RectangleShape,
    clip: Boolean = false
) = this.then(
    SimpleDrawLayerModifier(
        scaleX = scaleX,
        scaleY = scaleY,
        alpha = alpha,
        translationX = translationX,
        translationY = translationY,
        shadowElevation = shadowElevation,
        rotationX = rotationX,
        rotationY = rotationY,
        rotationZ = rotationZ,
        cameraDistance = cameraDistance,
        transformOrigin = transformOrigin,
        shape = shape,
        clip = clip,
        inspectorInfo = debugInspectorInfo {
            name = "drawLayer"
            properties["scaleX"] = scaleX
            properties["scaleY"] = scaleY
            properties["alpha"] = alpha
            properties["translationX"] = translationX
            properties["translationY"] = translationY
            properties["shadowElevation"] = shadowElevation
            properties["rotationX"] = rotationX
            properties["rotationY"] = rotationY
            properties["rotationZ"] = rotationZ
            properties["cameraDistance"] = cameraDistance
            properties["transformOrigin"] = transformOrigin
            properties["shape"] = shape
            properties["clip"] = clip
        }
    )
)

/**
 * A [Modifier.Element] that makes content draw into a draw layer. The draw layer can be
 * invalidated separately from parents. A [drawLayer] should be used when the content
 * updates independently from anything above it to minimize the invalidated content.
 *
 * [drawLayer] can be used to apply effects to content, such as scaling, rotation, opacity,
 * shadow, and clipping.
 * Prefer this version when you have layer properties backed by a
 * [androidx.compose.runtime.State] or an animated value as reading a state inside [block] will
 * only cause the layer properties update without triggering recomposition and relayout.
 *
 * @sample androidx.compose.ui.samples.AnimateFadeIn
 *
 * @param block block on [GraphicsLayerScope] where you define the layer properties.
 */
@Stable
fun Modifier.drawLayer(block: GraphicsLayerScope.() -> Unit): Modifier =
    this.then(
        BlockDrawLayerModifier(
            block = block,
            inspectorInfo = debugInspectorInfo {
                name = "drawLayer"
                properties["block"] = block
            }
        )
    )

internal interface DrawLayerModifier : Modifier.Element {
    val block: GraphicsLayerScope.() -> Unit
}

private class BlockDrawLayerModifier(
    override val block: GraphicsLayerScope.() -> Unit,
    inspectorInfo: InspectorInfo.() -> Unit
) : DrawLayerModifier, InspectorValueInfo(inspectorInfo) {

    override fun equals(other: Any?): Boolean {
        if (other !is BlockDrawLayerModifier) return false
        return block == other.block
    }

    override fun hashCode(): Int {
        return block.hashCode()
    }

    override fun toString(): String =
        "BlockDrawLayerModifier(" +
            "block=$block)"
}

private class SimpleDrawLayerModifier(
    private val scaleX: Float,
    private val scaleY: Float,
    private val alpha: Float,
    private val translationX: Float,
    private val translationY: Float,
    private val shadowElevation: Float,
    private val rotationX: Float,
    private val rotationY: Float,
    private val rotationZ: Float,
    private val cameraDistance: Float,
    private val transformOrigin: TransformOrigin,
    private val shape: Shape,
    private val clip: Boolean,
    inspectorInfo: InspectorInfo.() -> Unit
) : DrawLayerModifier, InspectorValueInfo(inspectorInfo) {

    override val block: GraphicsLayerScope.() -> Unit = {
        scaleX = this@SimpleDrawLayerModifier.scaleX
        scaleY = this@SimpleDrawLayerModifier.scaleY
        alpha = this@SimpleDrawLayerModifier.alpha
        translationX = this@SimpleDrawLayerModifier.translationX
        translationY = this@SimpleDrawLayerModifier.translationY
        shadowElevation = this@SimpleDrawLayerModifier.shadowElevation
        rotationX = this@SimpleDrawLayerModifier.rotationX
        rotationY = this@SimpleDrawLayerModifier.rotationY
        rotationZ = this@SimpleDrawLayerModifier.rotationZ
        cameraDistance = this@SimpleDrawLayerModifier.cameraDistance
        transformOrigin = this@SimpleDrawLayerModifier.transformOrigin
        shape = this@SimpleDrawLayerModifier.shape
        clip = this@SimpleDrawLayerModifier.clip
    }

    override fun hashCode(): Int {
        var result = scaleX.hashCode()
        result = 31 * result + scaleY.hashCode()
        result = 31 * result + alpha.hashCode()
        result = 31 * result + translationX.hashCode()
        result = 31 * result + translationY.hashCode()
        result = 31 * result + shadowElevation.hashCode()
        result = 31 * result + rotationX.hashCode()
        result = 31 * result + rotationY.hashCode()
        result = 31 * result + rotationZ.hashCode()
        result = 31 * result + cameraDistance.hashCode()
        result = 31 * result + transformOrigin.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + clip.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? SimpleDrawLayerModifier ?: return false
        return scaleX == otherModifier.scaleX &&
            scaleY == otherModifier.scaleY &&
            alpha == otherModifier.alpha &&
            translationX == otherModifier.translationX &&
            translationY == otherModifier.translationY &&
            shadowElevation == otherModifier.shadowElevation &&
            rotationX == otherModifier.rotationX &&
            rotationY == otherModifier.rotationY &&
            rotationZ == otherModifier.rotationZ &&
            cameraDistance == otherModifier.cameraDistance &&
            transformOrigin == otherModifier.transformOrigin &&
            shape == otherModifier.shape &&
            clip == otherModifier.clip
    }

    override fun toString(): String =
        "SimpleDrawLayerModifier(" +
            "scaleX=$scaleX, " +
            "scaleY=$scaleY, " +
            "alpha = $alpha, " +
            "translationX=$translationX, " +
            "translationY=$translationY, " +
            "shadowElevation=$shadowElevation, " +
            "rotationX=$rotationX, " +
            "rotationY=$rotationY, " +
            "rotationZ=$rotationZ, " +
            "cameraDistance=$cameraDistance, " +
            "transformOrigin=$transformOrigin, " +
            "shape=$shape, " +
            "clip=$clip)"
}
