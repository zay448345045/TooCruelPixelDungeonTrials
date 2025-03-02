package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Assets
import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.Statistics
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor
import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.Bundle
import com.watabou.utils.PointF
import java.util.Collections

class RacingTheDeath : Buff(), Hero.Doom {
    var particles: Emitter.Factory = PurpleParticle.BURST
    private lateinit var trailCells: MutableList<Int>
    private var trail: MutableList<Image>? = null
    private var lastDepth: Int = -1
    private var fx = false

    init {
        type = buffType.NEUTRAL
        resetTrail()
    }

    override fun detach() {
        //This buff can't be detached
    }

    override fun attachTo(target: Char): Boolean {
        if (target is Hero) {
            return super.attachTo(target)
        }
        return false
    }

    fun tick() {
        if (Dungeon.depth != lastDepth) {
            resetTrail()
            lastDepth = Dungeon.depth
        }

        var damage: Int = (Statistics.deepestFloor + 1) * 3

        trailCells.removeAt(TRAIL_LENGTH - 1)

        if (trailCells.contains(target.pos) && trailCells.indexOf(target.pos) != 0) {
            if (trailCells[3] == target.pos) {
                damage *= 4
                burst(target.pos, 30)
            } else {
                damage *= 2
                burst(target.pos, 15)
            }
        } else {
            val frequency: Int = Collections.frequency(trailCells, target.pos)
            damage = (frequency - 3) * damage * 3 / 2
            if (damage > 0) {
                burst(target.pos, 7 * (frequency - 3))
            }
        }

        trailCells.add(0, target.pos)
        addTrailSegment(target.pos)

        updateTrail()

        if (damage > 0) {
            target.damage(damage, this)
        }
    }

    override fun fx(on: Boolean) {
        fx = on
        if (on) {
            createTrail()
        } else {
            eraseTrail()
        }
    }

    private fun burst(pos: Int, amount: Int) {
        if (trail == null) return
        val i = trailCells.indexOf(pos)
        if (i == -1) return

        val sprite: Image = trail!![i]

        val emitter: Emitter = GameScene.emitter()
        emitter.pos(PointF(sprite.x + sprite.width / 2, sprite.y + sprite.height / 2))

        emitter.burst(particles, amount)

        Sample.INSTANCE.play(Assets.Sounds.LIGHTNING)
    }

    private fun resetTrail() {
        trailCells = mutableListOf()
        for (i in 0 until TRAIL_LENGTH) {
            trailCells.add(0)
        }
        //to make buff create first piece of trail right away
        spend(-1f)
        //To make sure that buff wont proc multiple times if function called more than once
        postpone(-1f)

        createTrail()
    }

    private fun updateTrail() {
        if (trail == null) return

        for (i in 0 until TRAIL_LENGTH) {
            val segment: Image = trail!![i]
            val pos = trailCells[i]

            segment.invert()

            segment.alpha(.3f)
            if (i == 4) {
                segment.alpha(.9f)
            }

            if (Actor.findChar(pos) != null) segment.alpha(segment.alpha() / 2)

            segment.visible = pos != Dungeon.hero.pos && Dungeon.level.visited[pos]
        }
    }

    private fun createTrail() {
        if (!fx) return

        if (trail != null) eraseTrail()

        if (Dungeon.depth != lastDepth) {
            lastDepth = Dungeon.depth
            resetTrail()
            return
        }
        trail = mutableListOf()

        for (i in 0 until TRAIL_LENGTH) {
            trail!!.add(Image())
        }
        for (i in 0 until TRAIL_LENGTH) {
            val pos = trailCells[i]
            if (pos != 0) {
                setTrailSegment(i, pos)
            }
        }

        updateTrail()
    }

    private fun addTrailSegment(pos: Int) {
        if (trail == null) return

        val segment: Image = HeroSprite.avatar(Dungeon.hero.heroClass, Dungeon.hero.tier())

        trail!![TRAIL_LENGTH - 1].killAndErase()
        trail!!.removeAt(TRAIL_LENGTH - 1)

        segment.point(worldToCamera(pos, segment))

        GameScene.effect(segment)
        trail!!.add(0, segment)
    }

    private fun setTrailSegment(i: Int, pos: Int) {
        if (trail == null) return

        val segment: Image = HeroSprite.avatar(Dungeon.hero.heroClass, Dungeon.hero.tier())

        trail!![i].killAndErase()
        trail!!.removeAt(i)

        segment.point(worldToCamera(pos, segment))

        GameScene.effect(segment)
        trail!!.add(i, segment)
    }

    private fun eraseTrail() {
        if (trail == null) return
        for (img in trail!!) {
            img.killAndErase()
        }
        trail = null
    }

    private fun worldToCamera(cell: Int, segment: Image): PointF {
        val csize = DungeonTilemap.SIZE

        return PointF(
            PixelScene.align(
                Camera.main,
                ((cell % Dungeon.level.width()) + 0.5f) * csize - segment.width * 0.5f
            ),
            PixelScene.align(
                Camera.main,
                ((cell / Dungeon.level.width()) + 1.0f) * csize - segment.height - csize * (6 / 16f)
            )
        )
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        val ret = IntArray(TRAIL_LENGTH)
        for (i in 0 until TRAIL_LENGTH) {
            ret[i] = trailCells[i]
        }

        bundle.put(TRAIL_CELLS, ret)

        bundle.put(DEPTH, lastDepth)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        val cells = bundle.getIntArray(TRAIL_CELLS)
        trailCells = mutableListOf()
        for (i in 0 until TRAIL_LENGTH) {
            if (i >= cells.size) {
                trailCells.add(0)
                continue
            }
            trailCells.add(cells[i])
        }

        lastDepth = bundle.getInt(DEPTH)
    }


    override fun onDeath() {
        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }

    companion object {
        private const val TRAIL_LENGTH = 10
        private const val TRAIL_CELLS = "trail_cells"
        private const val DEPTH = "last_depth"
    }
}