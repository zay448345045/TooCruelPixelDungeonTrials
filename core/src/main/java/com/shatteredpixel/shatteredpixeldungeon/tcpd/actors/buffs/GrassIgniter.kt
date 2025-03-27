package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level.igniteAllGrass

class GrassIgniter : Buff() {
    override fun act(): Boolean {
        Dungeon.level.igniteAllGrass()
        spend(TICK)
        return true
    }
}
