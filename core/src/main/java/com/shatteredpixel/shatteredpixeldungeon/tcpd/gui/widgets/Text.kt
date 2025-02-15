package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Widget
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock
import kotlin.math.ceil

class UiText(val text: String, val size: Int, val multiline: Boolean) : Widget {
    override fun ui(ui: Ui): UiResponse {
        val top = ui.top()
        val space = top.remainingSpace()
        val text = top.painter().drawText(space, text, size, multiline) as RenderedTextBlock

        val res = top.allocateSize(

            Vec2(ceil(text.width()).toInt(), ceil(text.height()).toInt())
        )
        return res
    }
}

fun Ui.label(text: String, size: Int, multiline: Boolean = false) {
    UiText(text, size, multiline).ui(this)
}