package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater
import com.shatteredpixel.shatteredpixeldungeon.items.Heap
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.watabou.utils.Random


fun Level.postCreateHook() {
    // Reveal all traps if THUNDERSTRUCK modifier is active, for fairness
    if(Modifier.THUNDERSTRUCK.active()) {
        for(trap in traps.valueList()) {
            trap.reveal()
        }
    }
    if(Modifier.SECOND_TRY.active()) {
        applySecondTry()
    }
}


private fun Level.applySecondTry() {
    var barricades = 0
    for (i in 0 until length()) {
        if (map[i] == Terrain.LOCKED_DOOR) {
            Level.set(i, Terrain.DOOR, this)
        }
        if (map[i] == Terrain.BARRICADE) {
            Level.set(i, Terrain.EMBERS, this)
            barricades++
        }
    }

    var h: Heap
    for (c in heaps.keyArray()) {
        h = heaps.get(c)
        if (h.type == Heap.Type.FOR_SALE) continue
        for (item in ArrayList<Item>(h.items)) {
            if (item.unique) continue
            if (!guaranteedItems.contains(item) || item is Key) h.items.remove(item)
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