package com.shatteredpixel.shatteredpixeldungeon.tcpd.windows

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifiers
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdWindow
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redCheckbox
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.verticalJustified

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
    redCheckbox(modifiers.isEnabled(modifier), modifier.localizedName(), 9).onClick {
        if (editable) {
            modifiers.toggle(modifier)
        }
    }
}