package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator


class RevengeFury : FlavourBuff(), AttackAmplificationBuff, DamageAmplificationBuff {
    override fun attackMultiplier(): Float {
        return 2f
    }

    override fun icon(): Int {
        return BuffIndicator.RAGE
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    override fun damageMultiplier(source: Any?): Float {
        if (target.properties().contains(Char.Property.BOSS)) return 1f
        return 1.2f
    }
}