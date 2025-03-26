package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Assets
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.updateFov
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class CasualApproach : Buff() {
    init {
        // act before hero turn
        actPriority = HERO_PRIO + 1
    }

    override fun act(): Boolean {
        val fov = target.updateFov(Dungeon.level)

        val validMobs = mutableListOf<Mob>()

        for (mob in Dungeon.level.mobs) {
            if (mob != target &&
                fov[mob.pos] &&
                mob.alignment != Alignment.ALLY &&
                mob !is NPC &&
                mob.buff(
                    ApproachUsed::class.java,
                ) == null
            ) {
                validMobs.add(mob)
            }
        }

        Random.element(validMobs)?.let {
            if (chain(it)) return false
        }

        spend(TICK)
        return true
    }

    private fun chain(enemy: Char): Boolean {
        if (Dungeon.level.adjacent(target.pos, enemy.pos) ||
            enemy
                .properties()
                .contains(Char.Property.IMMOVABLE)
        ) {
            if (enemy is Mob) {
                enemy.beckon(target.pos)
            }
            affect(enemy, ApproachUsed::class.java)
            return false
        }

        val target = enemy.pos

        val pos = this.target.pos
        val chain = Ballistica(pos, target, Ballistica.STOP_TARGET)

        if (chain.collisionPos != enemy.pos || Dungeon.level.pit[chain.path[1]]) {
            return false
        } else {
            var newPos = -1
            for (i in chain.subPath(1, chain.dist)) {
                if (!Dungeon.level.solid[i] && findChar(i) == null) {
                    newPos = i
                    break
                }
            }

            if (newPos == -1) {
                return false
            } else {
                val newPosFinal = newPos

                val sprite = this.target.sprite
                Item().throwSound()
                Sample.INSTANCE.play(Assets.Sounds.CHAINS)
                sprite.parent.add(
                    Chains(
                        DungeonTilemap.raisedTileCenterToWorld(pos),
                        enemy.sprite.destinationCenter(),
                        Effects.Type.CHAIN,
                    ) {
                        add(
                            Pushing(
                                enemy,
                                enemy.pos,
                                newPosFinal,
                            ) { pullEnemy(enemy, newPosFinal) },
                        )
                        next()
                    },
                )
            }
        }
        return true
    }

    private fun pullEnemy(
        enemy: Char,
        pullPos: Int,
    ) {
        affect(enemy, ApproachUsed::class.java)
        enemy.pos = pullPos
        enemy.sprite.place(pullPos)
        Dungeon.level.occupyCell(enemy)
        if (enemy === Dungeon.hero) {
            Dungeon.hero.interrupt()
            Dungeon.observe()
            GameScene.updateFog()
        } else if (enemy is Mob) {
            enemy.beckon(target.pos)
        }
        FullSceneUpdater.requestFog()
    }

    class ApproachUsed : Buff()
}
