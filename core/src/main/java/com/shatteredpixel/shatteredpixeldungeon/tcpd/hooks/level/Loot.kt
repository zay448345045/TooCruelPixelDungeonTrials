package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater
import com.shatteredpixel.shatteredpixeldungeon.items.Generator
import com.shatteredpixel.shatteredpixeldungeon.items.Heap
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.set
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.curseIfAllowed
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.defaultNItems
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.furrowCell
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.isLevelBossOrSpecial
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.transformItems
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.LevelCreationHooks
import com.watabou.utils.Random

@LevelCreationHooks
fun Level.applySecondTry() {
    if (isLevelBossOrSpecial()) {
        return
    }

    var barricades = 0
    for (i in 0 until length()) {
        if (map[i] == Terrain.LOCKED_DOOR) {
            set(i, Terrain.DOOR, this)
        }
        if (map[i] == Terrain.BARRICADE) {
            set(i, Terrain.EMBERS, this)
            barricades++
        }
    }

    var h: Heap
    for (c in heaps.keyArray()) {
        h = heaps.get(c)
        if (h.type == Heap.Type.FOR_SALE) continue
        for (item in ArrayList<Item>(h.items)) {
            // Only remove regular keys, not subclasses
            if (item.javaClass == Key::class.java) h.items.remove(item)
            if (item.unique) continue
            if (!guaranteedItems.contains(item)) h.items.remove(item)
            if (item is PotionOfLiquidFlame && barricades-- > 0) h.items.remove(item)
        }
        if (h.items.isEmpty()) {
            heaps.remove(c)
        }
    }

    for (blob in blobs.values) {
        if (blob is WellWater) {
            blob.fullyClear(this)
        }
    }
}

@LevelCreationHooks
fun Level.applyLootParadise() {
    val validCells = mutableListOf<Int>()
    for (i in 0 until length()) {
        if (!solid[i] && !pit[i]) {
            validCells.add(i)
        }
    }
    for (t in transitions) {
        validCells.remove(t.cell())
    }
    Random.shuffle(validCells)
    val amount = defaultNItems() * 10
    for (i in 0 until amount) {
        if (validCells.size <= i) break
        val cell = validCells[i]

        val toDrop = Generator.random() ?: continue
        furrowCell(cell)
        drop(toDrop, cell)
    }
}

@LevelCreationHooks
fun Level.applyCursed() {
    transformItems {
        it.curseIfAllowed(true)
        it
    }
}
