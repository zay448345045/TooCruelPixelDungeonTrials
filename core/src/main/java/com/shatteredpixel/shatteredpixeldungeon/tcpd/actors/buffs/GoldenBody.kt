package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.badlogic.gdx.math.MathUtils
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

class GoldenBody : NoDetachShieldBuff() {
    init {
        // Act after the Hero and other healing effects
        actPriority = HERO_PRIO - 2
    }

    override fun attachTo(target: Char?): Boolean {
        return super.attachTo(target).also { attached ->
            if (attached) {
                if (this.target.HP == this.target.HP && shielding() == 0) {
                    this.target.HP = 1
                    this.incShield(this.target.HT)
                }
            }
        }
    }

    // https://www.desmos.com/calculator/t14jd9rcyd
    // Approach double of HT with shield, but never reach it
    // Early curve remains mostly the same, so high level hero will have
    // roughly the same shield amount at low gold as the low level hero, but
    // has higher max shield
    private fun goldToShield(gold: Int): Int {
        if (gold == 0) return 0
        val upperLimit = target.HT * ASIMPT_LIMIT - 1
        val s = ASIMPT_LIMIT * target.HT
        return min((2.0 * s / (1.0 + E.pow(-gold / (dungeonScaling() * s))) - s).toInt(), upperLimit)
    }

    private fun shieldToGold(shield: Int): Int {
        if (shield == 0) return 0
        val max = target.HT * ASIMPT_LIMIT - 1
        if(shield > max) return shieldToGold(max)
        val s = ASIMPT_LIMIT * target.HT
        return (-dungeonScaling() * s * ln(2.0 * s / (shield.toDouble() + s) - 1.0)).toInt()
    }

    private fun healingToGoldRatio(): Int {
        return Dungeon.scalingDepth() / 5 + 1
    }

    private fun dungeonScaling(): Double {
        return CURVE * (1.0 + Dungeon.scalingDepth() / 50.0)
    }

    private fun recalculate() {
        val goldShield = shielding()
        val shielding = super.shielding()
        if (goldShield > shielding) {
            super.incShield(goldShield - shielding)
        } else if (goldShield < shielding) {
            super.decShield(shielding - goldShield)
        }
    }

    override fun act(): Boolean {
        recalculate()
        if (target.HP > 1) {
            val addGold = (target.HP - 1) * healingToGoldRatio()
            target.HP = 1
            // Only give gold if the shield is less than the HT
            if(shielding() < target.HT) {
                Dungeon.gold += addGold

                target.sprite?.showStatusWithIcon(
                    CharSprite.NEUTRAL,
                    addGold.toString(),
                    FloatingText.GOLD
                )
            }
        }
        SteelBody.wakeUp(target)
        spend(TICK)
        return true
    }

    override fun incShield(amt: Int) {
        val shield = shielding()
        val max = target.HT * ASIMPT_LIMIT - 1
        if (shield >= max) {
            return
        }
        val newShielding = MathUtils.clamp(shield + amt, shield, max)
        val newGold = shieldToGold(newShielding)
        Dungeon.gold += newGold - Dungeon.gold
        recalculate()
    }

    override fun decShield(amt: Int) {
        if (amt == 0) return
        val shield = shielding()
        if (amt >= shield) {
            Dungeon.gold = 0
            super.decShield(super.shielding())
            return
        }
        val gold = shieldToGold(shield)

        val newShielding = shield - amt

        val newGold = shieldToGold(newShielding)

        Dungeon.gold -= gold - newGold

        if (Dungeon.gold < 0) {
            Dungeon.gold = 0
        }
        recalculate()
    }

    override fun shielding(): Int {
        return goldToShield(Dungeon.gold)
    }

    companion object {
        private const val ASIMPT_LIMIT = 2

        // Picked by manually tweaking desmos graph
        private const val CURVE = 4.57f
    }
}