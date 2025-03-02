package com.shatteredpixel.shatteredpixeldungeon.tcpd.windows

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifiers
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdWindow
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.descriptor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redButton

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.verticalJustified
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons

open class WndModifiers(private val modifiers: Modifiers, private val editable: Boolean) :
    TcpdWindow() {
    override fun Ui.drawUi() {
        verticalJustified {
            label(Messages.get(this, "title"), 12).widget.hardlight(TITLE_COLOR)
            Modifier.entries.forEach {
                modifierBtn(modifiers, it, editable)
            }
        }
    }
}

fun Ui.modifierBtn(modifiers: Modifiers, modifier: Modifier, editable: Boolean) {
    redButton {
        label(modifier.localizedName(), 9)
        val ui = top();
        val space = ui.layout.getFullAvailableSpace()
        val checkboxRect = Rect.fromSize(Pos2(space.right() - 12 - 1, space.top() - 2), Vec2(12, 12));
        ui.painter().drawImage(
            ui.nextAutoId(),
            checkboxRect,
            if (modifiers.isEnabled(modifier)) {
                Icons.CHECKED
            } else {
                Icons.UNCHECKED
            }.descriptor()
        )
    }.onClick {
        if (editable) {
            modifiers.toggle(modifier)
        }
    }
}