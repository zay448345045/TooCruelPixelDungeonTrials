package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.Challenges
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.asBits
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.asBytes
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.assertEq
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.decodeBase58
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.encodeToBase58String
import com.shatteredpixel.shatteredpixeldungeon.tcpd.utils.trimEnd
import com.watabou.utils.BArray
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.DeviceCompat

enum class Modifier(
    val id: Int,
    locString: String? = null,
    val dependencies: Array<Int> = emptyArray(),
) {
    // Vanilla challenges
    CHAMPION_ENEMIES(7, locString = "champion_enemies"),
    COLOSSEUM(32, dependencies = arrayOf(CHAMPION_ENEMIES.id)),
    STRONGER_BOSSES(8, locString = "stronger_bosses"),
    ON_DIET(0, locString = "no_food"),
    FAITH_ARMOR(1, locString = "no_armor"),
    PHARMACOPHOBIA(2, locString = "no_healing"),
    BARREN_LAND(3, locString = "no_herbalism") {
        override fun _isItemBlocked(item: Item): Boolean = item is Dewdrop
    },
    DROUGHT(71),
    SWARM_INTELLIGENCE(4, locString = "swarm_intelligence"),
    DARKNESS(5, locString = "darkness"),
    FORBIDDEN_RUNES(6, locString = "no_scrolls"),

    // Custom content!
    CARDINAL_DISABILITY(9),
    RACING_THE_DEATH(10),
    HORDE(11) {
        override fun _nMobsMult(): Float = 2f
    },
    INVASION(12),
    GREAT_MIGRATION(13, dependencies = arrayOf(INVASION.id)),
    MUTAGEN(14),
    EVOLUTION(15, dependencies = arrayOf(MUTAGEN.id)) {
        override fun _isItemBlocked(item: Item): Boolean = item is RatSkull
    },
    ROTTEN_LUCK(16),
    ARROWHEAD(17),
    THUNDERSTRUCK(18, dependencies = arrayOf(ARROWHEAD.id)),
    SECOND_TRY(19),
    CRYSTAL_SHELTER(20),
    CRYSTAL_BLOOD(21),
    DEEPER_DANGER(22),
    HEAD_START(23),
    PRISON_EXPRESS(66, dependencies = arrayOf(HEAD_START.id)),
    BLINDNESS(24),
    BLOODBAG(25),
    REVENGE(26),
    REVENGE_FURY(27),
    PREPARED_ENEMIES(28),
    REPEATER(29),
    DUPLICATOR(30),
    EXTREME_CAUTION(31) {
        override fun _nTrapsMult(): Float = 4f
    },
    PATRON_SAINTS(33),
    PERSISTENT_SAINTS(34, dependencies = arrayOf(PATRON_SAINTS.id)),
    HOLY_WATER(35),
    INTOXICATION(36),
    PLAGUE(37),
    TOXIC_WATER(38),
    CERTAINTY_OF_STEEL(39) {
        override fun _isItemBlocked(item: Item): Boolean = item is SaltCube
    },
    GOLDEN_COLOSSUS(52, dependencies = arrayOf(CERTAINTY_OF_STEEL.id)) {
        override fun _isItemBlocked(item: Item): Boolean = item is MasterThievesArmband
    },
    PARADOX_LEVELGEN(40),
    RETIERED(41),
    UNTIERED(42, dependencies = arrayOf(RETIERED.id)),
    UNSTABLE_ACCESSORIES(43),
    PANDEMONIUM(44),
    BARRIER_BREAKER(45),
    MOLES(46),
    LOFT(47),
    BULKY_FRAME(48),
    SHROUDING_PRESENCE(56, dependencies = arrayOf(BULKY_FRAME.id)),
    SLIDING(49) {
        override fun _nWaterMult(): Float = 1.5f
    },
    INSOMNIA(50),
    LOOT_PARADISE(51),
    BOMBERMOB(53),
    CONSTELLATION(62, dependencies = arrayOf(BOMBERMOB.id)),
    CURSED(54),
    CURSE_MAGNET(55),
    EXTERMINATION(57),
    POSTPAID_LOOT(58, dependencies = arrayOf(EXTERMINATION.id)),
    MIMICS(59),
    MIMICS_ALL(60, dependencies = arrayOf(MIMICS.id)),
    MIMICS_GRIND(61, dependencies = arrayOf(MIMICS.id)),
    REPOPULATION(63),
    RESURRECTION(64, dependencies = arrayOf(REPOPULATION.id)),
    FRACTAL_HIVE(65, dependencies = arrayOf(REPOPULATION.id)),
    CROOKED_DIE(67),
    CRUMBLED_STAIRS(68),
    MULTICLASSING(69),
    EXOTIC_GOODS(70) {
        override fun _isItemBlocked(item: Item): Boolean = item is ExoticCrystals
    },
    ;

    companion object {
        val ALL: Array<Modifier> = Modifier.entries.sortedBy { it.id }.toTypedArray()

        init {
            if (ALL.last().id != ALL.size - 1) {
                val seen = mutableMapOf<Int, Modifier>()
                for (mod in ALL) {
                    val existing = seen[mod.id]
                    if (existing != null) {
                        throw IllegalStateException("Modifier id ${mod.id} is in use by both ${mod.name} and ${existing.name}")
                    }
                    seen[mod.id] = mod
                }
                throw IllegalStateException("Modifier IDs contain gaps!")
            }
        }

        fun fromVanilla(challengeId: Int): Modifier =
            when (challengeId) {
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

    private val localizationKey = locString ?: name.lowercase()
    private val localizationClass =
        if (locString == null) Modifier::class.java else Challenges::class.java

    fun localizedName(): String = Messages.get(localizationClass, localizationKey)

    fun localizedDesc(): String = Messages.get(localizationClass, localizationKey + "_desc")

    @Suppress("FunctionName")
    open fun _isItemBlocked(item: Item): Boolean = false

    @Suppress("FunctionName")
    open fun _nMobsMult(): Float = 1f

    @Suppress("FunctionName")
    open fun _nWaterMult(): Float = 1f

    @Suppress("FunctionName")
    open fun _nGrassMult(): Float = 1f

    @Suppress("FunctionName")
    open fun _nTrapsMult(): Float = 1f

    fun active() = Dungeon.tcpdData?.modifiers?.isEnabled(this) ?: false
}

class Modifiers() : Bundlable {
    private val modifiers: BooleanArray = BooleanArray(Modifier.entries.size)

    constructor(modifiers: BooleanArray) : this() {
        modifiers.copyInto(this.modifiers)
    }

    constructor(vararg modifiers: Modifier) : this() {
        for (modifier in modifiers) {
            enable(modifier)
        }
    }

    companion object {
        fun deserializeFromString(encoded: String): Modifiers {
            val bits = decodeBits(encoded)
            return Modifiers(bits.copyOf(Modifier.entries.size))
        }

        fun encodeBits(bits: BooleanArray): String {
            val encoded = bits.asBytes(false).trimEnd().encodeToBase58String()
            if (encoded.all { it == '1' }) return ""
            return encoded
        }

        fun decodeBits(encoded: String): BooleanArray = encoded.decodeBase58().asBits().trimEnd(false)

        fun debugModeActive(): Boolean = DeviceCompat.isDebug()

        const val MODIFIERS = "modifiers"

        init {
            doTests()
        }
    }

    fun asRaw(): BooleanArray = modifiers.copyOf()

    fun isChallenged(): Boolean = modifiers.any { it }

    fun activeChallengesCount(): Int = modifiers.count { it }

    fun isEnabled(modifier: Modifier): Boolean = modifiers[modifier.id]

    fun isVanillaEnabled(challengeId: Int): Boolean = isEnabled(Modifier.fromVanilla(challengeId))

    fun enable(modifier: Modifier) {
        modifiers[modifier.id] = true

        modifier.dependencies.forEach {
            enable(Modifier.ALL[it])
        }
    }

    fun disable(modifier: Modifier) {
        modifiers[modifier.id] = false

        for (mod in Modifier.entries) {
            if (mod.dependencies.contains(modifier.id)) {
                disable(mod)
            }
        }
    }

    fun toggle(modifier: Modifier) {
        if (isEnabled(modifier)) {
            disable(modifier)
        } else {
            enable(modifier)
        }
    }

    fun enableFrom(other: Modifiers) {
        BArray.setFalse(modifiers)
        for (entry in Modifier.entries) {
            if (other.isEnabled(entry)) {
                enable(entry)
            }
        }
    }

    fun disableAll() {
        BArray.setFalse(modifiers)
    }

    fun isItemBlocked(item: Item): Boolean = Modifier.entries.any { modifiers[it.id] && it._isItemBlocked(item) }

    fun isActionBanned(
        item: Item,
        action: String,
    ): Boolean {
        if (item.cursed && isEnabled(Modifier.CURSE_MAGNET)) {
            return action == Item.AC_DROP || action == Item.AC_THROW
        }
        return false
    }

    fun nMobsMult(): Float {
        var mult = 1f
        for (modifier in Modifier.entries) {
            if (modifiers[modifier.id]) {
                mult *= modifier._nMobsMult()
            }
        }
        return mult
    }

    fun nWaterMult(): Float {
        var mult = 1f
        for (modifier in Modifier.entries) {
            if (modifiers[modifier.id]) {
                mult *= modifier._nWaterMult()
            }
        }
        return mult
    }

    fun nGrassMult(): Float {
        var mult = 1f
        for (modifier in Modifier.entries) {
            if (modifiers[modifier.id]) {
                mult *= modifier._nGrassMult()
            }
        }
        return mult
    }

    fun nTrapsMult(): Float {
        var mult = 1f
        for (modifier in Modifier.entries) {
            if (modifiers[modifier.id]) {
                mult *= modifier._nTrapsMult()
            }
        }
        return mult
    }

    fun scalingDepthBonus(): Int = if (isEnabled(Modifier.DEEPER_DANGER)) 10 else 0

    override fun restoreFromBundle(bundle: Bundle) {
        bundle.getBooleanArray(MODIFIERS).copyInto(modifiers)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(MODIFIERS, modifiers)
    }

    fun serializeToString(): String {
        val str = encodeBits(modifiers)
        return str
    }
}

private fun doTests() {
    val mods =
        booleanArrayOf(
            true,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
        )
    val newMods = booleanArrayOf(true, false, true)

    val ogBytes = mods.asBytes(false).trimEnd()
    val newBytes = newMods.asBytes(false).trimEnd()

    val ogBase58 = ogBytes.encodeToBase58String()
    val newBase58 = newBytes.encodeToBase58String()

    assertEq(ogBase58, newBase58)
}
