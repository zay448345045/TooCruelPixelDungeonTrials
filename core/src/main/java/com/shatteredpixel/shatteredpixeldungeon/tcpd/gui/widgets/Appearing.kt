package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks.AnimationState
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.WidgetResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.TextureDescriptor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.easeOutBack
import com.watabou.noosa.Image
import kotlin.math.roundToInt

fun Ui.appearingIconButton(
    image: TextureDescriptor,
    show: Boolean? = null,
    duration: Float = 0.2f,
    easingAppear: ((Float) -> Float)? = ::easeOutBack,
    easingDisappear: ((Float) -> Float)? = ::easeOutBack,
) = appearingIconButton(image, show, duration, easingAppear, easingDisappear) { _, _ -> }

inline fun Ui.appearingIconButton(
    image: TextureDescriptor,
    show: Boolean? = null,
    duration: Float = 0.2f,
    noinline easingAppear: ((Float) -> Float)? = ::easeOutBack,
    noinline easingDisappear: ((Float) -> Float)? = ::easeOutBack,
    crossinline extraAnimation: (Image, Float) -> Unit,
): InteractiveResponse<Image> =
    customButton { interaction ->
        val top = top()
        val res =
            appearingIcon(image, show, duration, easingAppear, easingDisappear, extraAnimation)
        highlightTouchedVisual(interaction, res, top.style().interactionAnimationDuration)
        dimInactiveVisual(res)
        res.widget
    }

fun Ui.appearingIcon(
    image: TextureDescriptor,
    show: Boolean? = null,
    duration: Float = 0.2f,
    easingAppear: ((Float) -> Float)? = ::easeOutBack,
    easingDisappear: ((Float) -> Float)? = ::easeOutBack,
) = appearingIcon(image, show, duration, easingAppear, easingDisappear) { _, _ -> }

@Suppress("NAME_SHADOWING")
inline fun Ui.appearingIcon(
    image: TextureDescriptor,
    show: Boolean? = null,
    duration: Float = 0.2f,
    noinline easingAppear: ((Float) -> Float)? = ::easeOutBack,
    noinline easingDisappear: ((Float) -> Float)? = ::easeOutBack,
    crossinline extraAnimation: (Image, Float) -> Unit,
): WidgetResponse<Image> {
    val show = show ?: top().isEnabled()
    val top = top()
    val showAnim =
        ctx().getOrPutMemory(top.id.with("showAnim")) {
            AnimationState(show).also {
                it.easingUp = easingAppear
                it.easingDown = easingDisappear
            }
        }
    val imageId = top.nextAutoId()

    val progress = showAnim.animate(show, duration) { it }
    val imageSize = image.size()
    val allocated = top.allocateSize(Vec2((imageSize.x * progress).roundToInt(), imageSize.y))

    val img = top.painter().drawImage(imageId, allocated.min, image)
    img.scale.set(progress)
    img.origin.set(img.width / 2f * progress, img.height / 2f)

    extraAnimation(img, progress)

    val res = WidgetResponse(img, UiResponse(allocated, imageId))
    return res
}
