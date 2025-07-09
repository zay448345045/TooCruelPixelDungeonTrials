package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.WidgetResponse
import com.watabou.noosa.BitmapText
import kotlin.math.ceil

class UiBitmapText(
    val text: String,
) {
    fun show(ui: Ui): WidgetResponse<BitmapText> {
        val top = ui.top()
        val space = top.layout.nextAvailableSpace(ui.top().style())
        val id = top.nextAutoId()
        val text = top.painter().drawBitmapText(id, space, text)

        val textSize = Vec2(ceil(text.width()).toInt(), ceil(text.height()).toInt())
        val rect = top.allocateSize(textSize)

        if (rect.width() > textSize.x || rect.height() > textSize.y) {
            val newRect = rect.centerInside(textSize)
            text.x = newRect.min.x.toFloat()
            text.y = newRect.min.y.toFloat()
            PixelScene.align(text)
        }

        return WidgetResponse(text, UiResponse(rect, id))
    }
}

fun Ui.bitmapLabel(text: String): WidgetResponse<BitmapText> = UiBitmapText(text).show(this)

fun Ui.activeBitmapLabel(text: String): WidgetResponse<BitmapText> {
    val res = UiBitmapText(text).show(this)
    dimInactiveVisual(res)
    return res
}
