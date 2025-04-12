package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
import com.watabou.utils.Bundle

class DelayedBeckon :
    Buff(),
    OnDamageTakenBuff {
    private var ticker = -1

    override fun act(): Boolean {
        if (ticker == 0) {
            val t = target
            if (t is Mob) {
                t.beckon(Dungeon.hero.pos)
                if (t is Mimic) {
                    t.forceBeckon(Dungeon.hero.pos)
                } else if (t is Statue) {
                    t.forceBeckon(Dungeon.hero.pos)
                }
            }
            diactivate()
            return true
        }
        if (ticker < 0) {
            if (Dungeon.level.distance(target.pos, Dungeon.hero.pos) <= NOTICE_DISTANCE &&
                hasFov(
                    target.pos,
                    Dungeon.hero.pos,
                )
            ) {
                start()
            }
        }
        if (ticker > 0) {
            target.sprite.showStatus(CharSprite.NEUTRAL, "$ticker...")
            ticker--
        }

        spend(TICK)
        return true
    }

    private fun start() {
        if (ticker < 0) ticker = NOTICE_TIMEOUT
    }

    override fun onDamageTaken(
        damage: Int,
        src: Any?,
    ) {
        for (mob in Dungeon.level.mobs) {
            if (Dungeon.level.distance(target.pos, mob.pos) <= NOTICE_DISTANCE &&
                hasFov(
                    target.pos,
                    mob.pos,
                )
            ) {
                mob.buff(DelayedBeckon::class.java)?.start()
            }
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TICKER, ticker)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ticker = bundle.getInt(TICKER)
    }

    companion object {
        private const val TICKER = "ticker"
        const val NOTICE_DISTANCE = 2
        const val NOTICE_TIMEOUT = 2

        private fun hasFov(
            a: Int,
            b: Int,
        ): Boolean =
            Ballistica(
                a,
                b,
                Ballistica.STOP_SOLID or Ballistica.STOP_TARGET,
            ).collisionPos == b
    }
}
