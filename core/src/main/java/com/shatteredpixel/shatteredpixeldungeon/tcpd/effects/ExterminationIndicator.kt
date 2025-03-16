package com.shatteredpixel.shatteredpixeldungeon.tcpd.effects

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.utils.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

class ExterminationIndicator(private val target: CharSprite) : Image() {
    init {
        copy(Icons.get(Icons.TARGET))
        originToCenter()
    }

    private var tMove = Random.Float(2 * PI.toFloat())
    private var tScale = Random.Float(2 * PI.toFloat())

    private val xFactor: Int = Random.Int(1, 7)
    private val yFactor: Int = run {
        var n: Int
        do {
            n = Random.Int(1, 7)
        } while (n == xFactor)
        n
    }

    private val tDirection = sign(Random.Float(-1f, 1f))
    private val angular = Random.Float(-0.5f, 0.5f).let { it + sign(it) * 0.75f }

    override fun update() {
        super.update()

        angle += Game.elapsed * 180f * angular
        tMove += Game.elapsed * tDirection
        tScale += Game.elapsed * sqrt(2f) * tDirection
        tMove %= (2 * PI.toFloat())
        tScale %= (2 * PI.toFloat())

        scale.set(cos(tScale) / 8 + 0.375f)

        visible = target.visible
        if (visible) {
            val p = target.center()
            point(
                p.x + cos(xFactor * tMove) * (target.width() - width()) / 2,
                p.y + sin(yFactor * tMove) * (target.height() - height()) / 2
            )
        }
    }

    fun point(x: Float, y: Float): ExterminationIndicator {
        this.x = x - (width / 2f)
        this.y = y - (height / 2f)
        return this
    }
}