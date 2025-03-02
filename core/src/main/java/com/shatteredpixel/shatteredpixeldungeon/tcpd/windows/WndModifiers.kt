package com.shatteredpixel.shatteredpixeldungeon.tcpd.windows

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifiers
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.TcpdWindow
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.descriptor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.iconButton
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redCheckbox
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.rightToLeft
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.verticalJustified
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage

open class WndModifiers(private val modifiers: Modifiers, private val editable: Boolean) :
    TcpdWindow() {
    override fun Ui.drawUi() {
        verticalJustified {
            label(Messages.get(WndModifiers::class.java, "title"), 12).widget.hardlight(TITLE_COLOR)
            top().addSpace(2)
            Modifier.entries.forEach {
                modifierBtn(modifiers, it, editable)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(editable) {
            SPDSettings.challenges(modifiers)
        }
    }
}

private fun Ui.modifierBtn(modifiers: Modifiers, modifier: Modifier, editable: Boolean) {
    rightToLeft {
        margins(Margins.only(top = 2)) {
            iconButton(Icons.INFO.descriptor()).onClick {
                ShatteredPixelDungeon.scene().add(
                    WndMessage(modifier.localizedDesc())
                )
            }
        }
        verticalJustified {
            redCheckbox(modifiers.isEnabled(modifier), modifier.localizedName(), 9).onClick {
                if (editable) {
                    modifiers.toggle(modifier)
                }
            }
        }
    }
}