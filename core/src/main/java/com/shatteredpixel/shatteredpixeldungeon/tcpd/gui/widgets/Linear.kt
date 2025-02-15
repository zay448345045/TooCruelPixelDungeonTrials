package com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets

import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.InnerResponse
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.LayoutDirection
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.layout.Ui
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.NinePatchDescriptor
import com.watabou.noosa.NinePatch

data class LinearLayout(
    val direction: LayoutDirection,
    val background: NinePatchDescriptor? = null,
) {
    inline fun <T> show(ui: Ui, block: () -> T): InnerResponse<T> {
        val background = if (background != null) {
            ui.top().painter().drawNinePatch(Rect.ZERO, background) as NinePatch
        } else {
            null
        }

        val margins = background?.margins() ?: Margins.ZERO

        val res = ui.withLayout(
            margins = margins,
            layout = direction,
            block = block
        )

        if (background != null) {
            background.x = res.response.rect.min.x.toFloat()
            background.y = res.response.rect.min.y.toFloat()
            background.size(res.response.rect.width().toFloat(), res.response.rect.height().toFloat())
        }

        return res
    }
}

inline fun <T> Ui.vertical(
    background: NinePatchDescriptor? = null, block: () -> T
): InnerResponse<T> {
    return LinearLayout(
        direction = LayoutDirection.VERTICAL,
        background = background,
    ).show(
        this,
        block
    )
}

inline fun <T> Ui.horizontal(
    background: NinePatchDescriptor? = null, block: () -> T
): InnerResponse<T> {
    return LinearLayout(
        direction = LayoutDirection.HORIZONTAL,
        background = background,
    ).show(this, block)
}

inline fun <T> Ui.stack(
    background: NinePatchDescriptor? = null, block: () -> T
): InnerResponse<T> {
    return LinearLayout(
        direction = LayoutDirection.STACK,
        background = background
    ).show(this, block)
}