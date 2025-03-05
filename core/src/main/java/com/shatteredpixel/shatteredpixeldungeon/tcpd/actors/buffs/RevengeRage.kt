package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.min


class RevengeRage : Buff(), AttackAmplificationBuff {
    private var boost = 0

    fun add(amount: Int) {
        boost += amount
        boost = min(boost.toDouble(), 9001.0).toInt()
    }

    init {
        type = buffType.POSITIVE
    }

    override fun flatAttackBonusPostMult(): Float {
        return boost.toFloat()
    }

    override fun icon(): Int {
        return BuffIndicator.FURY
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", boost)
    }

    override fun fx(on: Boolean) {
        if (on) {
            target.sprite.emit(RevengeRage::class.java).pour(WindParticle.FACTORY(0xFF0000), 0.1f)
        } else {
            target.sprite.killEmitter(RevengeRage::class.java)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(BOOST, boost)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        boost = bundle.getInt(BOOST)
    }

    companion object {
        private const val BOOST = "attack_boost"
    }
}