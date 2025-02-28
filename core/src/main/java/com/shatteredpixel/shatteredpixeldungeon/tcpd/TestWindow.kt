package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.Chrome
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Pos2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Rect
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.Vec2
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.context.Context
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.hooks.useState
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.painter.descriptor
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.horizontal
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.label
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.margins
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.redButton
import com.shatteredpixel.shatteredpixeldungeon.tcpd.gui.widgets.vertical
import com.shatteredpixel.shatteredpixeldungeon.ui.Window
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog
import com.watabou.noosa.Game
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class TestWindow : Window(0, 0, Chrome.get(Chrome.Type.WINDOW)) {
    private val ctx = Context()

    init {
        this.addToFront(ctx.rootGroup)
    }

    override fun update() {
        val maxSize = Rect.fromSize(
            Pos2(0, 0),
            Vec2(
                120,
                Int.MAX_VALUE
            )
        )
        val res = ctx.update(maxSize) {
            var counter by useState(Unit) { 0 }

            vertical {

                val sizeBoost = sin((Game.realTime / 200).toDouble() / PI) * 5 + 5

                label("Hello, World!", 9 + sizeBoost.roundToInt())
                label("This is a test window.", 9)
                label("Current time ${Game.realTime/1000}", 9)
                label("Size boost: ${sizeBoost.roundToInt()}", 9)
                label(
                    "This is a long text that should wrap around the window. lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    9,
                    true
                )

                redButton("Counter is at $counter!").onClick {
                    GLog.i("Button clicked!")
                    counter++
                }

                horizontal(background = Chrome.Type.RED_BUTTON.descriptor()) {
                    margins(Margins.symmetric(0, 2)) {
                        label("Button 124", 9)
                        label("Button 456", 16)
                    }
                }
            }
        }
        val size = res.response.rect.size()
        resize(size.x, size.y)
        super.update()
    }
}