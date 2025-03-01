package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Widget
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock
import kotlin.math.ceil

class UiText(val text: String, val size: Int, val multiline: Boolean) : Widget {
    override fun ui(ui: Ui): UiResponse {
        val top = ui.top()
        val space = top.layout.nextAvailableSpace(ui.top().style())
        val text = top.painter().drawText(space, text, size, multiline) as RenderedTextBlock

        val textSize = Vec2(ceil(text.width()).toInt(), ceil(text.height()).toInt());
        val res = top.allocateSize(textSize)

        if (res.rect.width() > textSize.x || res.rect.height() > textSize.y) {
            val newRect = res.rect.centerInside(textSize)
            text.setPos(newRect.min.x.toFloat(), newRect.min.y.toFloat())
            PixelScene.align(text)
        }
        return res
    }
}

fun Ui.label(text: String, size: Int, multiline: Boolean = false) {
    UiText(text, size, multiline).ui(this)
}