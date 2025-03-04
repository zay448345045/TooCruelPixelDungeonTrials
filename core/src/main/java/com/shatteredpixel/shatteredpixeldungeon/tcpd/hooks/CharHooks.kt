

package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.Challenges
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Arrowhead
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Arrowhead.MobArrowhead
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.DamageAmplificationBuff


/**
 * Hook which is called when mob takes damage, but before any damage
 * reduction/amplification is applied.
 */
fun Char.incomingDamageHook(dmg: Int, src: Any?): Int {
    return dmg
}

@Suppress("NAME_SHADOWING")
fun Char.damageMultiplierHook(dmg: Int, damage: Float, src: Any?): Float {
    var damage = damage
    for(buff in buffs()) {
        if(buff is DamageAmplificationBuff) {
            damage *= buff.damageMultiplier(src)
        }
    }
    return damage
}

/**
 * Hook which is called when char takes damage, after all damage
 * reduction/amplification is applied, and HP was reduced.
 */
fun Char.damageTakenHook(dmg: Int, shielded: Int, src: Any?): Int {
    return dmg
}

/**
 * Hook which is called when char dies.
 */
fun Char.deathHook(src: Any?) {
    if (this is Mob) {
        if(Modifier.ARROWHEAD.active()) {
            Buff.affect(Dungeon.hero, Arrowhead::class.java).addStack()
        }
    }
}

/**
 * Same as [Char.damageTakenHook], but called only for mobs.
 */
fun Mob.mobIncomingDamageHook(dmg: Int, src: Any?): Int {
    if(Modifier.CARDINAL_DISABILITY.active()) {
        if(alignment == Char.Alignment.ENEMY) {
            val selfPos = Dungeon.level.cellToPoint(pos)
            val heroPos = Dungeon.level.cellToPoint(Dungeon.hero.pos)
            if(selfPos.x != heroPos.x && selfPos.y != heroPos.y) {
                return -1
            }
        }
    }

    return dmg
}

fun Mob.mobFirstAdded() {
    if (Modifier.ARROWHEAD.active()) {
        Buff.affect(this, MobArrowhead::class.java)
    }
}