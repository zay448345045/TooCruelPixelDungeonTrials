package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.InnerResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui

class Margins(val margins: Margins) {
    inline fun <T> show(ui: Ui, block: () -> T): InnerResponse<T> {
        return ui.withLayout(
            margins = margins,
            block = block
        )
    }
}

inline fun <T> Ui.margins(margins: Margins, block: ()->T): InnerResponse<T> {
    return Margins(margins).show(this, block)
}