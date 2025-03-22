package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator

class InvisibleResting : Buff() {
    override fun act(): Boolean {
        val t = target
        val sleeping =
            (t is Hero && t.resting) ||
                (t is Mob && t.state == t.SLEEPING) ||
                t.buff(MagicalSleep::class.java) != null ||
                t.buff(Sleep::class.java) != null

        if (sleeping) {
            prolong(t, Hiding::class.java, 1f)
        }

        spend(TICK)
        return true
    }

    class Hiding : Invisibility() {
        init {
            announced = false
            // act after the main buff to avoid flickering
            actPriority = BUFF_PRIO - 1
        }

        override fun icon(): Int = BuffIndicator.NONE
    }
}
