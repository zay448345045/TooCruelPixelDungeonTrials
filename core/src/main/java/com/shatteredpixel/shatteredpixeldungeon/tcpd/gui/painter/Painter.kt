package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter

import com.shatteredpixel.shatteredpixeldungeon.Chrome
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock
import com.watabou.gltextures.SmartTexture
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.Gizmo
import com.watabou.noosa.Group
import com.watabou.noosa.Image
import com.watabou.noosa.NinePatch

class Painter internal constructor(
    private val group: Group?,
    private val cache: PaintCache,
) {
    companion object {
        fun create(group: Group?, cache: PaintCache): Painter {
            cache.restart()
            return Painter(group, cache)
        }
    }

    /**
     * Returns a new painter with the new clip rect.
     *
     * The new clip rect is the intersection of the current clip rect and the given clip rect.
     *
     * @param clip The new clip rect.
     */
    fun clipped(clip: Rect?): Painter {
        // We are already inside non-visible area.
        if (this.group == null) {
            return this
        }

        val group = add(VisualElement.Group) as Group
        group.clear()
        val cam = this.group.camera().withClip(clip) ?: return Painter(null, cache)
        group.camera = cam

        return Painter(group, cache)
    }

    fun drawRect(rect: Rect, color: Int) {
        add(VisualElement.ColoredRect(rect, color))
    }

    fun drawNinePatch(rect: Rect, descriptor: NinePatchDescriptor): Gizmo {
        return add(VisualElement.NinePatch(rect, descriptor))
    }

    fun drawImage(rect: Rect, texture: TextureDescriptor): Gizmo {
        return add(VisualElement.Image(rect, texture))
    }

    fun drawText(rect: Rect, text: String, size: Int, multiline: Boolean): Gizmo {
        return add(VisualElement.Text(rect, text, size, multiline))
    }

    private fun add(element: VisualElement): Gizmo {
        val gizmo = cache.advance(element);

        group?.addToFront(gizmo)
        return gizmo
    }
}

internal sealed class VisualElement {
    class ColoredRect(val rect: Rect, val color: Int) : VisualElement()
    class NinePatch(val rect: Rect, val descriptor: NinePatchDescriptor) :
        VisualElement()

    class Image(val rect: Rect, val texture: TextureDescriptor) : VisualElement()
    class Text(val rect: Rect, val text: String, val size: Int, val multiline: Boolean) :
        VisualElement()

    data object Group : VisualElement()

    fun asGizmo(cached: Pair<VisualElement, Gizmo>?): Gizmo {
        when (this) {
            is ColoredRect -> {
                val block = if (cached?.first is ColoredRect && cached.second is ColorBlock) {
                    val old = cached.first as ColoredRect
                    if (old.color == color) {
                        return cached.second as ColorBlock
                    } else {
                        null
                    }
                } else {
                    null
                } ?: ColorBlock(rect.width().toFloat(), rect.height().toFloat(), color)

                block.origin.set(rect.min.x.toFloat(), rect.min.y.toFloat())
                block.size(rect.width().toFloat(), rect.height().toFloat())
                return block
            }

            is Image -> {
                val image = if (cached?.second is com.watabou.noosa.Image) {
                    val image = cached.second as com.watabou.noosa.Image

                    image.texture(texture.asKey())
                    image
                } else {
                    Image(texture.asKey())
                }
                image.width = rect.width().toFloat()
                image.height = rect.height().toFloat()
                image.origin.set(rect.min.x.toFloat(), rect.min.y.toFloat())
                return image
            }

            is NinePatch -> {
                val block =
                    if (cached?.first is NinePatch && cached.second is com.watabou.noosa.NinePatch) {
                        val old = cached.first as NinePatch
                        if (old.descriptor == descriptor) {
                            val block = cached.second as com.watabou.noosa.NinePatch
                            block
                        } else {
                            null
                        }
                    } else {
                        null
                    } ?: descriptor.get()

                block.x = rect.min.x.toFloat()
                block.y = rect.min.y.toFloat()
                block.size(rect.width().toFloat(), rect.height().toFloat())
                return block
            }

            is Group -> {
                if (cached?.second is com.watabou.noosa.Group) {
                    return cached.second as com.watabou.noosa.Group
                }

                return Group()
            }

            is Text -> {
                if (cached?.first is Text && cached.second is RenderedTextBlock) {
                    var block = cached.second as RenderedTextBlock
                    val old = cached.first as Text

                    block.text(text)
                    if (multiline) {
                        block.maxWidth(rect.width().toInt())
                    } else {
                        if (old.multiline) {
                            block = PixelScene.renderTextBlock(text, size)
                        }
                    }
                    block.setPos(rect.min.x.toFloat(), rect.min.y.toFloat())
                    return block
                }
                val block = PixelScene.renderTextBlock(text, size)

                if (multiline) {
                    block.maxWidth(rect.width())
                }
                block.setPos(rect.min.x.toFloat(), rect.min.y.toFloat())
                return block
            }
        }
    }
}

sealed interface TextureDescriptor {
    class ByName(val name: String) : TextureDescriptor
    class SmartTexture(val texture: com.watabou.gltextures.SmartTexture) : TextureDescriptor
    class Pixmap(val pixmap: com.badlogic.gdx.graphics.Pixmap) : TextureDescriptor

    fun asKey(): Any {
        return when (this) {
            is ByName -> name
            is SmartTexture -> texture
            is Pixmap -> pixmap
        }
    }
}

sealed interface NinePatchDescriptor {
    data class Chrome(val type: com.shatteredpixel.shatteredpixeldungeon.Chrome.Type) :
        NinePatchDescriptor

    fun get(): NinePatch {
        return when (this) {
            is Chrome -> com.shatteredpixel.shatteredpixeldungeon.Chrome.get(type)
        }
    }
}

fun Chrome.Type.descriptor(): NinePatchDescriptor {
    return NinePatchDescriptor.Chrome(this)
}

fun TextureCache.get(obj: TextureDescriptor): SmartTexture {
    throw Error("Do not use TextureCache.get directly with TextureDescriptor. Use `asKey` first.")
}