package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import kotlin.math.min


class PermaBlind : Buff() {
    init {
        actPriority = HERO_PRIO - 1
    }

    private var turnsToConfuse = CONFUSE_TIMEOUT
    override fun act(): Boolean {
        var weak = true
        for (c in PathFinder.NEIGHBOURS8) {
            val cell: Int = target.pos + c
            if (Dungeon.level.solid[cell] || Dungeon.level.pit[cell]) {
                weak = false
                turnsToConfuse = min((turnsToConfuse + 1).toDouble(), CONFUSE_TIMEOUT.toDouble())
                    .toInt()
            }
        }
        if (weak) {
            prolong(target, Blindness::class.java, 1f)
            if (--turnsToConfuse <= 0) {
                prolong(target, Vertigo::class.java, 1f)
            }
        }
        spend(TICK)
        return true
    }


    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(CONFUSE, turnsToConfuse)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        turnsToConfuse = bundle.getInt(CONFUSE)
    }

    companion object {
        private const val CONFUSE_TIMEOUT = 3
        private const val CONFUSE = "confuse"
    }
}