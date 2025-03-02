package com.shatteredpixel.shatteredpixeldungeon.tcpd.windows

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifiers
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdWindow
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redButton

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.verticalJustified

open class WndModifiers(val modifiers: Modifiers, val editable: Boolean): TcpdWindow() {
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
        if (modifiers.isEnabled(modifier)) {
            label(Messages.get(this, "enabled"), 9).widget.hardlight(0xFF0000)
        }
    }.onClick {
        if (editable) {
            modifiers.toggle(modifier)
        }
    }
}