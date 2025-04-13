package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.set
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PATRON_SEED_BLESS
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PatronSaintsBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.LevelCreationHooks

@LevelCreationHooks
fun Level.applyThunderstruck() {
    // Reveal all traps if THUNDERSTRUCK modifier is active, for fairness
    for (trap in traps.valueList()) {
        trap.reveal()
        if (map[trap.pos] == Terrain.SECRET_TRAP) {
            set(trap.pos, Terrain.TRAP, this)
        }
    }
}

@LevelCreationHooks
fun Level.applyHolyWater() {
    for (i in 0 until length()) {
        if (map[i] == Terrain.WATER) {
            Blob.seed(i, PATRON_SEED_BLESS, PatronSaintsBlob::class.java, this)
        }
    }
}

@LevelCreationHooks
fun Level.applyLoft() {
    for (i in 0 until length()) {
        if ((map[i] == Terrain.WALL || map[i] == Terrain.WALL_DECO) && insideMap(i)) {
            map[i] = Terrain.CHASM
        } else if (!insideMap(i)) {
            map[i] = Terrain.CHASM
        }
    }
    buildFlagMaps()
    cleanWalls()
}
