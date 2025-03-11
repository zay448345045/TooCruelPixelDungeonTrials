package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Assets
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class InsomniaSlowdown : FlavourBuff(), TimescaleBuff {
    override fun speedFactor(): Float {
        return 0.5f
    }

    companion object {
        var DURATION: Float = 5f
    }
}

class InsomniaSpeed : Buff(), TimescaleBuff {
    override fun speedFactor(): Float {
        return 2f
    }

    override fun act(): Boolean {
        if (target is Mob) {
            val mob = target as Mob
            if (mob.state == mob.SLEEPING) {
                mob.beckon(mob.pos)
            }
        }
        return super.act()
    }
}

class Insomnia : Buff() {
    override fun act(): Boolean {
        if (target is Hero) {
            val hero = target as Hero
            if (hero.resting) {
                hero.interrupt()
                Sample.INSTANCE.play(Assets.Sounds.CURSED)
                repeat(4) {
                    Wraith.spawnAt(hero.pos)?.let { wraith ->
                        affect(wraith, DelayedDecay::class.java).set(
                            delay = Random.IntRange(2, 6) * TICK,
                            fx = true,
                            invulnerable = true
                        )
                        affect(wraith, AscensionChallenge.AscensionBuffBlocker::class.java)
                    }
                }
                GLog.n(Messages.get(this, "wakeup"))
            }
        } else {
            detach()
        }
        spend(TICK)
        return true
    }
}