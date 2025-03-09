package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Arrowhead
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.ControlledRandomness
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Intoxication
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Pandemonium
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.PermaBlind
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.RacingTheDeath
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.RetieredBuff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.SteelBody

fun Hero.heroLiveHook() {
    if (Modifier.RACING_THE_DEATH.active()) {
        Buff.affect(this, RacingTheDeath::class.java)
    }
    if (Modifier.THUNDERSTRUCK.active()) {
        Buff.affect(this, Arrowhead::class.java).set(9001)
    }
    if (Modifier.BLINDNESS.active()) {
        Buff.affect(this, PermaBlind::class.java)
    }
    if (Modifier.TOXIC_WATER.active()) {
        Buff.affect(this, Intoxication.ToxicWaterTracker::class.java)
    }
    if (Modifier.CERTAINTY_OF_STEEL.active()) {
        Buff.affect(this, SteelBody::class.java)
    }
    if (Modifier.RETIERED.active()) {
        Buff.affect(this, RetieredBuff::class.java)
    }
    if(Modifier.UNSTABLE_ACCESSORIES.active()) {
        Buff.affect(this, ControlledRandomness::class.java)
    }
    if(Modifier.PANDEMONIUM.active()) {
        Buff.affect(this, Pandemonium::class.java)
    }
}

fun Hero.heroSpendConstantHook(time: Float) {
    if (time > 0) {
        buff(RacingTheDeath::class.java)?.tick()
    }
}

fun Hero.wandProcHook(target:Char, wand: Wand, chargesUsed: Int) {

}

fun Hero.wandUsedHook(wand: Wand) {
    if(Modifier.PANDEMONIUM.active()) {
        buff(Pandemonium::class.java)?.wandUsed(wand)
    }
}

fun hungerDisabled(): Boolean {
    return Modifier.CERTAINTY_OF_STEEL.active()
}

fun regenerationDisabled(): Boolean {
    return Modifier.CERTAINTY_OF_STEEL.active()
}