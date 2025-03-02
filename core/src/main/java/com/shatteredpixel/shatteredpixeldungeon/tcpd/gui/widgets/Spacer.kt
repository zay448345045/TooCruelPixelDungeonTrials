package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.UiResponse

class Spacer(val size: Vec2) {
    fun show(ui: Ui): UiResponse {
        val rect = ui.top().allocateSize(size)
        return UiResponse(rect, ui.top().nextAutoId())
    }
}

fun Ui.spacer(size: Vec2): UiResponse {
    return Spacer(size).show(this)
}