package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.Level.set
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.ExterminationItemLock
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PATRON_SEED_BLESS
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PatronSaintsBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Exterminating
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.LevelCreationHooks
import com.watabou.utils.BArray
import com.watabou.utils.PathFinder

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

@LevelCreationHooks
fun Level.applyExtermination() {
    // Don't exterminate on boss levels
    if (Dungeon.bossLevel()) return

    val exterminateItemHolders = Modifier.MIMICS.active()

    val requireReachable = Modifier.POSTPAID_LOOT.active()
    if (requireReachable) {
        PathFinder.buildDistanceMap(
            getTransition(null).cell(),
            BArray.or(passable, avoid, null),
        )
    }
    if (Modifier.POSTPAID_LOOT.active()) {
        val lock = ExterminationItemLock()
        blobs[ExterminationItemLock::class.java] = lock
        for (h in heaps.valueList()) {
            lock.lockItem(this, h)
        }

        for (mob in mobs.toTypedArray()) {
            if (mob is Mimic) lock.lockMimic(this, mob)
            if (mob is Statue) lock.lockStatue(this, mob)
        }
    }
    for (m in mobs) {
        if (!exterminateItemHolders && (m is Mimic || m is Statue)) continue
        if (requireReachable && PathFinder.distance[m.pos] == Integer.MAX_VALUE) continue
        Buff.affect(m, Exterminating::class.java)
    }
}
