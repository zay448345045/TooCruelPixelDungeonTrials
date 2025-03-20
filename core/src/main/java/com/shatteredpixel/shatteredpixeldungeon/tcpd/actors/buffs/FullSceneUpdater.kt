package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import kotlin.math.min

class FullSceneUpdater : Buff() {
    init {
        // act before everything, so even the VFX update properly
        actPriority = VFX_PRIO + 1
    }

    var walls = false
    var fog = false

    companion object {
        fun requestFog() {
            request(walls = false, fog = true)
        }

        fun requestFull() {
            request(walls = true, fog = true)
        }

        private fun request(
            walls: Boolean,
            fog: Boolean,
        ) {
            if (Dungeon.hero == null) {
                return
            }
            val buff = affect(Dungeon.hero, FullSceneUpdater::class.java)
            buff.walls = buff.walls || walls
            buff.fog = buff.fog || fog
            buff.timeToNow()
            // Spend a tiny amount of time to ensure the buff is processed after the current turn
            // If The hero cooldown is negative for some reason, snap the time to right before
            // it, to ensure the buff is processed before the hero acts
            buff.spendConstant(min(0.1f, Dungeon.hero.cooldown() - 0.1f))
        }
    }

    override fun act(): Boolean {
        if (walls) {
            Dungeon.level.buildFlagMaps()
            Dungeon.level.cleanWalls()
        }
        if (fog) {
            Dungeon.observe(Dungeon.level.length())
            GameScene.updateMap()
        }

//        if(DeviceCompat.isDebug()) {
//            GLog.i("FullSceneUpdater: updated scene at " + now())
//        }

        diactivate()
        return true
    }
}
