package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level

import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.LevelCreationHooks
import com.watabou.utils.Random

@LevelCreationHooks
fun Level.applyDrought() {
    val chance = 0.8f
    var terrain: Int
    for (i in 0 until length()) {
        terrain = map[i]
        val isGrass =
            terrain == Terrain.GRASS || terrain == Terrain.HIGH_GRASS || terrain == Terrain.FURROWED_GRASS

        if ((isGrass || terrain == Terrain.WATER) && Random.Float() < chance) {
            if (isGrass) {
                map[i] = Terrain.EMBERS
            }
            map[i] = Terrain.EMPTY
        }
    }

    buildFlagMaps()
}
