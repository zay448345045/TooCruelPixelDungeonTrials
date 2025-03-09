package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.transmuteInventoryItem
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Pandemonium : Buff(), AttackProcBuff {
    private var weaponRerollChance = WEP_CHANCE_BASE
    private var wandRerollChance = WAND_CHANCE_BASE

    override fun attachTo(target: Char?): Boolean {
        if (target !is Hero) {
            return false
        }
        return super.attachTo(target)
    }

    fun wandUsed(wand: Wand) {
        if (Random.Float() < wandRerollChance) {
            wandRerollChance = WAND_CHANCE_BASE
            transmuteInventoryItem(wand, target as Hero)
        } else {
            wandRerollChance += WAND_CHANCE_INCREMENT
        }
    }

    override fun attackProc(enemy: Char, damage: Int) {
        val hero = target as Hero

        if (Random.Float() < weaponRerollChance) {
            weaponRerollChance = WEP_CHANCE_BASE
            reroll(hero.belongings.weapon(), hero.belongings.secondWep())
        } else {
            weaponRerollChance += WEP_CHANCE_INCREMENT
        }
    }

    private fun reroll(vararg items: Item?) {
        val hero = target as Hero

        for (item in items) {
            if (item != null) {
                transmuteInventoryItem(item, hero)
            }
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(WEP_REROLL_CHANCE, weaponRerollChance)
        bundle.put(WAND_REROLL_CHANCE, wandRerollChance)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        weaponRerollChance = bundle.getFloat(WEP_REROLL_CHANCE)
        wandRerollChance = bundle.getFloat(WAND_REROLL_CHANCE)
    }

    companion object {
        private const val WEP_CHANCE_BASE = 0.05f
        private const val WEP_CHANCE_INCREMENT = 0.05f
        private const val WAND_CHANCE_BASE = 0.1f
        private const val WAND_CHANCE_INCREMENT = 0.1f

        private const val WEP_REROLL_CHANCE = "weapon_reroll_chance"
        private const val WAND_REROLL_CHANCE = "wand_reroll_chance"
    }
}
