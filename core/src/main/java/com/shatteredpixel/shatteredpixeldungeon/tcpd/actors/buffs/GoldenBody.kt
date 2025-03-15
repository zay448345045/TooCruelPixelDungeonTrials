package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite

class GoldenBody : NoDetachShieldBuff() {
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

    private fun shieldToGoldRation(): Int {
        return 10 + Dungeon.scalingDepth() / 5
    }

    private fun healingToGoldRation(): Int {
        return Dungeon.scalingDepth() / 5 + 1
    }

    private fun recalculateGold() {
        val goldShield = Dungeon.gold
        val shielding = super.shielding()
        if (goldShield > shielding) {
            super.incShield(goldShield - shielding)
        } else if (goldShield < shielding) {
            super.decShield(shielding - goldShield)
        }
    }

    override fun act(): Boolean {
        recalculateGold()
        if (target.HP > 1) {
            val addGold = (target.HP - 1) * healingToGoldRation()
            target.HP = 1
            Dungeon.gold += addGold

            target.sprite?.showStatusWithIcon(
                CharSprite.NEUTRAL,
                addGold.toString(),
                FloatingText.GOLD
            )
        }
        SteelBody.wakeUp(target)
        spend(TICK)
        return true
    }

    override fun incShield(amt: Int) {
        val addGold = amt * shieldToGoldRation()
        Dungeon.gold += addGold
        target.sprite?.showStatusWithIcon(
            CharSprite.NEUTRAL,
            addGold.toString(),
            FloatingText.GOLD
        )
        recalculateGold()
    }

    override fun decShield(amt: Int) {
        Dungeon.gold -= amt * shieldToGoldRation()
        if (Dungeon.gold < 0) {
            Dungeon.gold = 0
        }
        recalculateGold()
    }

    override fun shielding(): Int {
        return Dungeon.gold / shieldToGoldRation()
    }
}