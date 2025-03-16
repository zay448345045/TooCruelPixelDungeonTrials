package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.mobs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC
import com.shatteredpixel.shatteredpixeldungeon.items.Heap
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap.Guardian
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.ExterminationItemLock
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Exterminating
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.HoldingHeap
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.isNoneOr
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.isSomeAnd
import com.watabou.utils.BArray
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import com.watabou.utils.Reflection


class HolderMimic : Mimic() {
    init {
        spriteClass = MimicSprite::class.java
    }

    override fun setLevel(level: Int) {
        if (Modifier.MIMICS_GRIND.active()) super.setLevel(level)
        super.setLevel(Math.round(level * 0.75f))
    }

    override fun generatePrize(useDecks: Boolean) {
        if (Modifier.MIMICS_GRIND.active()) super.generatePrize(useDecks)
        // no prize
    }
}

class HolderStatue : Statue() {
    init {
        spriteClass = StatueSprite::class.java

        //reduced HP
        HT = 10 + Dungeon.depth * 3
        HP = HT
    }
}

class StoredHeapData : Bundlable {
    var isLevelGenStatue = false
    var extraMimicLoot = false
    var holderClass: Class<out Mob>? = null

    val childHeaps: MutableList<StoredHeapData> = mutableListOf()

    var heapType: Heap.Type? = null
    val items: MutableList<Item> = ArrayList()

    @Suppress("NAME_SHADOWING")
    fun restoreAtPos(level: Level, pos: Int) {
        var pos = pos
        val heapConflict =
            level.heaps[pos].isSomeAnd { it.type != Heap.Type.HEAP || heapType.isSomeAnd { ty -> ty != Heap.Type.HEAP } }

        val avoidMobs = holderClass != null
        if ((holderClass == null && heapConflict) || (avoidMobs && Actor.findChar(
                pos
            ) != null)
        ) {
            PathFinder.buildDistanceMap(
                pos, BArray.or(level.passable, level.avoid, null)
            )
            val validCells = HashSet<Int>()
            var minDistance = Int.MAX_VALUE
            var m: Mob
            for (i in PathFinder.distance.indices) {
                val dist = PathFinder.distance[i]
                if (dist <= minDistance) {
                    if (level.heaps.containsKey(i) || !(level.passable[i] || level.avoid[i]) || level.pit[i]) {
                        continue
                    }
                    if ((level.findMob(i).also { m = it }) != null && (avoidMobs || m.properties()
                            .contains(Char.Property.IMMOVABLE) || m is NPC)
                    ) {
                        continue
                    }
                    if (dist == minDistance) {
                        validCells.add(i)
                    } else {
                        validCells.clear()
                        validCells.add(i)
                        minDistance = dist
                    }
                }
            }
            if (validCells.size > 0) {
                pos = Random.element(validCells)
            } else {
                // no valid cells, just proceed with the original position and hope for the best
            }
        }

        if (holderClass != null) {
            var cl = holderClass!!
            val mob: Mob
            val wepIdx = items.indexOfFirst { it is MeleeWeapon }
            var armorIdx = items.indexOfFirst { it is Armor }
            // Holder statues can be converted to holder mimics if they have no weapon
            if (cl == HolderStatue::class.java && wepIdx < 0) {
                cl = HolderMimic::class.java
            } else {
                // Armored statues can be converted to statues if they have no armor
                if (cl == ArmoredStatue::class.java && armorIdx < 0) {
                    cl = Statue::class.java
                }

                // Regular statues convert into regular mimics if they have no weapon
                if (Statue::class.java.isAssignableFrom(cl) && wepIdx < 0) {
                    cl = Mimic::class.java
                }
            }
            if (Mimic::class.java.isAssignableFrom(cl) && cl != HolderMimic::class.java) {
                mob = Mimic.spawnAt(pos, cl, false)
            } else {
                when (cl) {
                    HolderMimic::class.java -> {
                        mob = HolderMimic()
                        mob.setLevel(Dungeon.scalingDepth())
                    }

                    HolderStatue::class.java -> {
                        mob = HolderStatue()
                        val wep = items.removeAt(wepIdx) as MeleeWeapon
                        mob.weapon = wep
                        mob.levelGenStatue = isLevelGenStatue
                    }

                    Statue::class.java -> {
                        mob = Statue()
                        val wep = items.removeAt(wepIdx) as MeleeWeapon

                        mob.weapon = wep
                        mob.levelGenStatue = isLevelGenStatue
                    }

                    Guardian::class.java -> {
                        mob = Guardian()
                        val wep = items.removeAt(wepIdx) as MeleeWeapon
                        mob.weapon = wep
                        mob.levelGenStatue = isLevelGenStatue
                    }

                    ArmoredStatue::class.java -> {
                        mob = ArmoredStatue()
                        val wep = items.removeAt(wepIdx) as MeleeWeapon
                        if (armorIdx > wepIdx) armorIdx--
                        val armor = items.removeAt(armorIdx) as Armor

                        mob.weapon = wep
                        mob.armor = armor
                        mob.levelGenStatue = isLevelGenStatue
                    }

                    else -> {
                        // attempt to create whatever mob we have
                        mob = Reflection.newInstance(cl)
                    }
                }
            }
            for (childHeap in childHeaps) {
                Buff.affect(mob, HoldingHeap::class.java).set(childHeap)
            }
            childHeaps.clear()
            val holderBuff = Buff.affect(mob, HoldingHeap::class.java).set(this.withoutHolder().copy())
            if (mob is Mimic && heapType.isNoneOr { it == Heap.Type.HEAP }) {
                if (mob.items == null) mob.items = ArrayList()
                if (extraMimicLoot) {
                    holderBuff.heap.items.addAll(mob.items)
                }
                mob.items.clear()
            }
            mob.pos = pos
            if (level == Dungeon.level) {
                GameScene.add(mob)
            } else {
                level.mobs.add(mob)
            }
        } else {
            for (childHeap in childHeaps) {
                childHeap.restoreAtPos(level, pos)
            }
            childHeaps.clear()
            if (items.isNotEmpty()) {
                var heap: Heap?

                do {
                    heap = level.drop(items.removeAt(0), pos)
                } while (heap.isNoneOr { it.isEmpty })

                if (heap != null) {
                    if (items.isNotEmpty()) {
                        heap.items.addAll(items)
                    }

                    if (heapType != null) {
                        heap.type = heapType
                        heap.sprite?.link()
                        heap.sprite?.drop()
                    }
                }
            }
        }
    }

    fun withoutHolder(): StoredHeapData {
        if (holderClass == null) {
            return this
        }

        val data = StoredHeapData()
        data.heapType = heapType
        data.items.addAll(items)
        return data
    }

    fun copy(): StoredHeapData {
        val b = Bundle()
        storeInBundle(b)
        return StoredHeapData().also { it.restoreFromBundle(b) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreFromBundle(bundle: Bundle) {
        if (bundle.contains(HEAP_TYPE)) {
            heapType = bundle.getEnum(HEAP_TYPE, Heap.Type::class.java)
        }
        isLevelGenStatue = bundle.getBoolean(IS_LEVEL_GEN_STATUE)
        extraMimicLoot = bundle.getBoolean(EXTRA_MIMIC_LOOT)
        if (bundle.contains(HOLDER_CLASS)) {
            val cl = bundle.getClass(HOLDER_CLASS);
            if (!Mob::class.java.isAssignableFrom(cl)) {
                throw IllegalArgumentException("Holder class must be a subclass of Mob")
            }
            holderClass = cl as Class<out Mob>
        }
        if (bundle.contains(CHILD_HEAPS)) {
            childHeaps.clear()
            childHeaps.addAll(bundle.getCollection(CHILD_HEAPS) as Collection<StoredHeapData>)
        }
        items.clear()
        items.addAll(bundle.getCollection(ITEMS) as Collection<Item>)
    }

    override fun storeInBundle(bundle: Bundle) {
        if (heapType != null) {
            bundle.put(HEAP_TYPE, heapType)
        }
        bundle.put(IS_LEVEL_GEN_STATUE, isLevelGenStatue)
        bundle.put(EXTRA_MIMIC_LOOT, extraMimicLoot)
        if (holderClass != null) {
            bundle.put(HOLDER_CLASS, holderClass)
        }
        if (childHeaps.isNotEmpty()) {
            bundle.put(CHILD_HEAPS, childHeaps)
        }
        bundle.put(ITEMS, items)
    }

    companion object {
        const val HEAP_TYPE = "heap_type"
        const val IS_LEVEL_GEN_STATUE = "is_level_gen_statue"
        const val EXTRA_MIMIC_LOOT = "extra_mimic_loot"
        const val HOLDER_CLASS = "holder_class"
        const val CHILD_HEAPS = "child_heaps"
        const val ITEMS = "items"

        fun fromHeap(heap: Heap): StoredHeapData {
            return StoredHeapData().also {
                it.items.addAll(heap.items)
                it.heapType = heap.type
            }
        }

        fun transformHeapIntoMimic(
            level: Level, heap: Heap, extraLoot: Boolean, weakHolders: Boolean
        ) {
            val data = fromHeap(heap)

            val cl = when (data.heapType!!) {
                Heap.Type.HEAP, Heap.Type.FOR_SALE, Heap.Type.CHEST, Heap.Type.TOMB, Heap.Type.SKELETON, Heap.Type.REMAINS -> {
                    val weaponIdx = data.items.indexOfFirst { it is MeleeWeapon }
                    var armorIdx = data.items.indexOfFirst { it is Armor }
                    if (weaponIdx >= 0) {
                        data.items.add(data.items.removeAt(weaponIdx))
                        if (armorIdx > weaponIdx) armorIdx--
                        if (weakHolders) {
                            HolderStatue::class.java
                        } else if (armorIdx >= 0) {
                            data.items.add(data.items.removeAt(armorIdx))
                            ArmoredStatue::class.java
                        } else {
                            Statue::class.java
                        }
                    } else if (weakHolders) {
                        HolderMimic::class.java
                    } else {
                        Mimic::class.java
                    }
                }

                Heap.Type.LOCKED_CHEST -> GoldenMimic::class.java
                Heap.Type.CRYSTAL_CHEST -> CrystalMimic::class.java
            }
            StoredHeapData().let {
                it.holderClass = cl
                it.childHeaps.add(data)
                it.extraMimicLoot = extraLoot
                it.restoreAtPos(level, heap.pos)
            }
        }

        fun fromMob(mob: Mob): StoredHeapData {
            return when (mob) {
                is Mimic -> {
                    fromMimic(mob)
                }

                is Statue -> {
                    fromStatue(mob)
                }

                else -> {
                    fromMobRaw(mob)
                }
            }
        }

        fun fromMimic(mimic: Mimic): StoredHeapData {
            return fromMobRaw(mimic).also {
                if (mimic.items != null) it.items.addAll(mimic.items)
                for (buff in mimic.buffs(HoldingHeap::class.java)) {
                    it.childHeaps.add(buff.heap)
                    buff.detach()
                }
            }
        }

        fun fromStatue(statue: Statue): StoredHeapData {
            return fromMobRaw(statue).also {
                it.items.add(statue.weapon)
                if (statue is ArmoredStatue) {
                    it.items.add(statue.armor)
                }
            }
        }

        private fun fromMobRaw(mob: Mob): StoredHeapData {
            return StoredHeapData().also {
                it.holderClass = mob.javaClass

                for (buff in mob.buffs(HoldingHeap::class.java)) {
                    it.childHeaps.add(buff.heap)
                    buff.detach()
                }
            }
        }
    }
}