package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff

class InvulnerableUntilSeen :
    Buff(),
    InvulnerabilityBuff {
    override fun act(): Boolean {
        if (Dungeon.level.heroFOV[target.pos] ||
            Dungeon.level.adjacent(
                target.pos,
                Dungeon.hero.pos,
            )
        ) {
            detach()
        }
        spend(TICK)
        return true
    }

    override fun isInvulnerable(effect: Class<out Any>): Boolean = true
}
