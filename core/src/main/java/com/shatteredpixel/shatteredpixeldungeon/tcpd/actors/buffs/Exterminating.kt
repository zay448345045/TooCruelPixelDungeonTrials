package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Assets
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.ExterminationItemLock
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.findBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.effects.ExterminationIndicator
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class Exterminating :
    Buff(),
    PersistHeapNestingBuff {
    var sprite: ExterminationIndicator? = null

    override fun fx(on: Boolean) {
        if (on) {
            if (sprite != null) sprite?.killAndErase()
            sprite = ExterminationIndicator(target.sprite)
            sprite?.hardlight(0.5f, 1f, 2f)
            GameScene.effect(sprite)
        } else {
            sprite?.killAndErase()
            sprite = null
        }
    }

    override fun detach() {
        super.detach()
        processExtermination(Dungeon.level)
    }

    class Reveal :
        FlavourBuff(),
        MindVisionExtBuff {
        companion object {
            val DURATION = TICK * 2f
        }

        override fun detach() {
            FullSceneUpdater.requestFog()
            super.detach()
        }

        override fun revealRadius(): Int = 1
    }

    override fun applyNestingEffect(target: Char) {
        affect(target, Exterminating::class.java)
    }

    companion object {
        fun processExtermination(level: Level) {
            for (mob in level.mobs) {
                if (mob.isAlive && mob.buff(Exterminating::class.java) != null) {
                    return // still not done
                }
            }
            if (level.findBlob<ExterminationItemLock>()?.unlockAll(level) == true) {
                GLog.p(Messages.get(Modifier::class.java, "extermination_complete"))
                Sample.INSTANCE.play(Assets.Sounds.LEVELUP, 0.5f, 1.5f)
            }
        }

        fun exterminationDone(level: Level): Boolean {
            if (!Modifier.EXTERMINATION.active()) return true
            var nExterminating = 0
            for (mob in level.mobs) {
                if (mob.isAlive && mob.buff(Exterminating::class.java) != null) {
                    nExterminating++
                    affect(mob, Reveal::class.java, Reveal.DURATION)
                }
            }
            if (nExterminating > 0) {
                if (nExterminating > 1) {
                    GLog.w(Messages.get(Modifier::class.java, "extermination_lock", nExterminating))
                } else {
                    GLog.w(Messages.get(Modifier::class.java, "extermination_lock_last"))
                }
                Dungeon.observe()
                GameScene.updateFog()
                Dungeon.hero.checkVisibleMobs()
                return false
            }

            return true
        }

        fun fallIntoPit(hero: Hero) {
            ScrollOfTeleportation.teleportChar(hero)
            Chasm.heroLand()
        }
    }
}
