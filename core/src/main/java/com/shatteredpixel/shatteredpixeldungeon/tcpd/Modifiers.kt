package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.Challenges
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.asBits
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.asBytes
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.decodeBase58
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.encodeToBase58String
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

enum class Modifier(val id: Int, locString: String? = null) {
    // Vanilla challenges
    ON_DIET(0, locString = "no_food"),
    FAITH_ARMOR(1, locString = "no_armor"),
    PHARMACOPHOBIA(2, locString = "no_healing"),
    BARREN_LAND(3, locString = "no_herbalism"),
    SWARM_INTELLIGENCE(4, locString = "swarm_intelligence"),
    DARKNESS(5, locString = "darkness"),
    FORBIDDEN_RUNES(6, locString = "no_scrolls"),
    CHAMPION_ENEMIES(7, locString = "champion_enemies"),
    STRONGER_BOSSES(8, locString = "stronger_bosses"),

    // Custom content!
    CARDINAL_DISABILITY(9),
    RACING_THE_DEATH(10),
    ;

    companion object {
        fun fromVanilla(challengeId: Int): Modifier {
            return when (challengeId) {
                Challenges.NO_FOOD -> ON_DIET
                Challenges.NO_ARMOR -> FAITH_ARMOR
                Challenges.NO_HEALING -> PHARMACOPHOBIA
                Challenges.NO_HERBALISM -> BARREN_LAND
                Challenges.SWARM_INTELLIGENCE -> SWARM_INTELLIGENCE
                Challenges.DARKNESS -> DARKNESS
                Challenges.NO_SCROLLS -> FORBIDDEN_RUNES
                Challenges.CHAMPION_ENEMIES -> CHAMPION_ENEMIES
                Challenges.STRONGER_BOSSES -> STRONGER_BOSSES
                else -> throw IllegalArgumentException("Unknown vanilla challenge id: $challengeId")
            }
        }
    }

    private val localizationKey = locString ?: name.lowercase()
    private val localizationClass = if(locString == null) Modifier::class.java else Challenges::class.java

    fun localizedName(): String {
        return Messages.get(localizationClass, localizationKey)
    }

    fun localizedDesc(): String {
        return Messages.get(localizationClass, localizationKey + "_desc")
        return Messages.get(localizationClass, localizationKey + "_desc")
    }

    open fun isItemBlocked(item: Item): Boolean {
        return false
    }

    fun active() = Dungeon.tcpdData.modifiers.isEnabled(this)
}

class Modifiers() : Bundlable {
    private val modifiers: BooleanArray = BooleanArray(Modifier.entries.size)
    constructor(modifiers: BooleanArray) : this() {
        modifiers.copyInto(this.modifiers)
    }

    companion object {
        fun deserializeFromString(encoded: String): Modifiers {
            val bits = encoded.decodeBase58().asBits()
            return Modifiers(bits.copyOf(Modifier.entries.size))
        }

        const val MODIFIERS = "modifiers"
    }

    fun isChallenged(): Boolean {
        return modifiers.any { it }
    }

    fun activeChallengesCount(): Int {
        return modifiers.count { it }
    }

    fun isEnabled(modifier: Modifier): Boolean {
        return modifiers[modifier.id]
    }

    fun isVanillaEnabled(challengeId: Int): Boolean {
        return isEnabled(Modifier.fromVanilla(challengeId))
    }

    fun enable(modifier: Modifier) {
        modifiers[modifier.id] = true
    }

    fun disable(modifier: Modifier) {
        modifiers[modifier.id] = false
    }

    fun toggle(modifier: Modifier) {
        modifiers[modifier.id] = !modifiers[modifier.id]
    }

    fun isItemBlocked(item: Item): Boolean {
        return Modifier.entries.any { modifiers[it.id] && it.isItemBlocked(item) }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        bundle.getBooleanArray(MODIFIERS).copyInto(modifiers)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(MODIFIERS, modifiers)
    }

    fun serializeToString(): String {
        return modifiers.asBytes(false).encodeToBase58String()
    }
}
