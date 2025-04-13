package com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.level

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.ExterminationItemLock
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.DelayedBeckon
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.Exterminating
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.LevelCreationHooks
import com.watabou.utils.BArray
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

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

@LevelCreationHooks
fun Level.applyInYourFace() {
    if (Dungeon.bossLevel()) return
    val entrance = getTransition(null)?.cell() ?: return

    val suitableCells = mutableListOf<Int>()

    for (o in PathFinder.NEIGHBOURS8) {
        val cell = entrance + o

        if (!solid[cell] &&
            !pit[cell] &&
            (passable[cell] || avoid[cell]) &&
            insideMap(cell) &&
            findMob(
                cell,
            ) == null
        ) {
            suitableCells.add(cell)
        }
    }
    Random.shuffle(suitableCells)

    var count = Random.NormalIntRange(1, 3)
    if (Dungeon.depth == 1) {
        count = 1
    }
    while (count-- > 0 && suitableCells.isNotEmpty()) {
        val cell = suitableCells.removeLast()
        val mob = createMob()
        mob.pos = cell
        mob.state = mob.PASSIVE
        Buff.affect(mob, DelayedBeckon::class.java).start(0)
        this.mobs.add(mob)
    }
}
