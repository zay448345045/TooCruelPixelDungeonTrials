package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Arrowhead
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Arrowhead.MobArrowhead
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.AtkSkillChangeBuff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.BloodbagBleeding
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.CrystalShield
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.DamageAmplificationBuff
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.DefSkillChangeBuff
import kotlin.math.max


/**
 * Hook which is called when mob takes damage, but before any damage
 * reduction/amplification is applied.
 */
fun Char.incomingDamageHook(dmg: Int, src: Any?): Int {
    if (dmg < 0) {
        return dmg
    }
    return dmg
}

/**
 * Hook which is called when calculating damage multipliers
 *
 * This is called before the champion damage multiplier is applied
 */
@Suppress("NAME_SHADOWING")
fun Char.damageMultiplierHook(dmg: Int, damage: Float, src: Any?): Float {
    var damage = damage
    for (buff in buffs()) {
        if (buff is DamageAmplificationBuff) {
            damage *= buff.damageMultiplier(src)
        }
    }
    return damage
}

/**
 * Hook which is called right before the damage is applied to the char's
 * shielding and, subsequently, HP
 */
fun Char.beforeDamageShieldedHook(dmg: Int, src: Any?): Int {
    if (CrystalShield.blockIncomingDamage(this, dmg)) {
        return -1
    }
    if (this is Hero && Modifier.BLOODBAG.active() && src !is Bleeding) {
        Buff.affect(this, BloodbagBleeding::class.java).add(max(dmg / 2f, 1f))
        return -1
    }
    return dmg
}

/**
 * Hook which is called when char takes damage, after all damage
 * reduction/amplification is applied, and HP was reduced.
 */
fun Char.damageTakenHook(dmg: Int, shielded: Int, src: Any?) {
    if (this is Mob) {
        if (HP <= 0) {
            if (Modifier.CRYSTAL_BLOOD.active() && buff(CrystalShield.DeathMarker::class.java) == null && !properties().contains(
                    Char.Property.BOSS
                )
            ) {
                HP = 1
                CrystalShield.applyShieldingLayers(this, 5, "barrier x5")
                Buff.affect(this, CrystalShield.DeathMarker::class.java)
            }
        }
    }
}

/**
 * Hook which is called when char dies.
 */
fun Char.deathHook(src: Any?) {
    if (this is Mob) {
        if (Modifier.ARROWHEAD.active()) {
            Buff.affect(Dungeon.hero, Arrowhead::class.java).addStack()
        }
    }
}

fun charHitAcuStatHook(attacker: Char, defender: Char, acuStat: Float): Float {
    var stat = acuStat
    for (buff in attacker.buffs()) {
        if (buff is AtkSkillChangeBuff) {
            stat = buff.modifyAtkSkill(stat, defender)
        }
    }
    return stat
}

fun charHitDefStatHook(attacker: Char, defender: Char, defStat: Float): Float {
    var stat = defStat
    for (buff in defender.buffs()) {
        if (buff is DefSkillChangeBuff) {
            stat = buff.modifyDefSkill(stat, attacker)
        }
    }
    return stat
}

fun charHitAcuRollHook(attacker: Char, defender: Char, acuRoll: Float): Float {
    var roll = acuRoll
    for (buff in attacker.buffs()) {
        if (buff is AtkSkillChangeBuff) {
            roll *= buff.atkRollMultiplier(attacker)
        }
    }
    return roll
}

fun charHitDefRollHook(attacker: Char, defender: Char, defRoll: Float): Float {
    var roll = defRoll
    for (buff in defender.buffs()) {
        if (buff is DefSkillChangeBuff) {
            roll *= buff.defRollMultiplier(attacker)
        }
    }
    return roll
}

/**
 * Same as [Char.damageTakenHook], but called only for mobs.
 */
fun Mob.mobIncomingDamageHook(dmg: Int, src: Any?): Int {
    if (Modifier.CARDINAL_DISABILITY.active()) {
        if (alignment == Char.Alignment.ENEMY) {
            val selfPos = Dungeon.level.cellToPoint(pos)
            val heroPos = Dungeon.level.cellToPoint(Dungeon.hero.pos)
            if (selfPos.x != heroPos.x && selfPos.y != heroPos.y) {
                return -1
            }
        }
    }

    return dmg
}

fun Mob.mobFirstAdded() {
    if (this is NPC) {
        return
    }
    if (Modifier.ARROWHEAD.active()) {
        Buff.affect(this, MobArrowhead::class.java)
    }
    if (Modifier.CRYSTAL_SHELTER.active()) {
        Buff.affect(this, CrystalShield::class.java)
        Buff.append(this, CrystalShield.Layer::class.java)
    }
}