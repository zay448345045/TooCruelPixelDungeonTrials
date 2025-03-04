package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle


class Arrowhead : Buff(), DamageAmplificationBuff {
    private val COOLDOWN = (if (Modifier.THUNDERSTRUCK.active()) 420 else 20).toFloat()
    private var stacks = 0

    init {
        type = buffType.POSITIVE
    }

    override fun act(): Boolean {
        stacks--
        if (stacks <= 0) {
            detach()
            return true
        }
        spend(COOLDOWN)
        return true
    }

    override fun icon(): Int {
        return BuffIndicator.FURY
    }

    override fun desc(): String {
        return Messages.get(
            this,
            "desc",
            stacks,
            Math.round(stacks * 100 * 0.3f),
            Math.round(stacks * 100 * 0.1f)
        )
    }

    override fun damageMultiplier(source: Any?): Float {
        if (source is Hunger) return 1f
        return 1 + stacks * 0.3f
    }

    fun addStack(): Arrowhead {
        stacks++
        postpone(COOLDOWN)
        return this
    }

    fun set(stacks: Int): Arrowhead {
        this.stacks = stacks
        postpone(COOLDOWN)
        return this
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STACKS, stacks)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        stacks = bundle.getInt(STACKS)
    }

    class MobArrowhead : Buff(), DamageAmplificationBuff {
        override fun damageMultiplier(source: Any?): Float {
            val arrowhead = Dungeon.hero.buff(Arrowhead::class.java) ?: return 1f
            return (1 + arrowhead.stacks * 0.1f)
        }
    }

    companion object {
        private const val STACKS = "arrowhead_stacks"
    }
}
