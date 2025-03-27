package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.curseIfAllowed

class ScorchedEarth {
    companion object {
        fun explode(
            target: Char,
            toCurse: Item?,
        ) {
            Bomb().explode(target.pos)
            val bal = Ballistica(target.pos, target.pos, Ballistica.PROJECTILE)
            CursedWand.randomValidEffect(null, target, bal, false)?.effect(null, target, bal, false)
            toCurse?.curseIfAllowed(true)
        }
    }
}
