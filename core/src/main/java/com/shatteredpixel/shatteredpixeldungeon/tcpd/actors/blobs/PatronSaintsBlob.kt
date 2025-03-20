package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle
import com.shatteredpixel.shatteredpixeldungeon.levels.Level
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs.PatronSaints
import com.shatteredpixel.shatteredpixeldungeon.tcpd.effects.CustomBlobCellEmission
import com.watabou.noosa.Game
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.DeviceCompat
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

const val STACKS_MASK: Int = 0b00001111
const val SOULS_MASK: Int = 0b11110000
const val MAX_STACKS: Int = 8

const val PATRON_SEED_SOUL: Int = 0x9870
const val PATRON_SEED_BLESS: Int = 0x9871

@Suppress("NOTHING_TO_INLINE")
@JvmInline
private value class EncodedCell(
    val rawValue: Int,
) {
    companion object {
        val EMPTY: EncodedCell = EncodedCell(0)

        inline fun from(
            souls: Int,
            stacks: Int,
        ): EncodedCell = EMPTY.withStacks(stacks).withSouls(souls)
    }

    inline fun withStacks(stacks: Int): EncodedCell {
        if (stacks > MAX_STACKS) {
            throw IllegalArgumentException("Stacks must be less or equal to $MAX_STACKS")
        }
        return EncodedCell(rawValue and STACKS_MASK.inv() or stacks)
    }

    inline fun stacks(): Int = rawValue and STACKS_MASK

    inline fun incrementStacks(): EncodedCell {
        if (stacks() == MAX_STACKS) {
            return this
        }
        return withStacks(stacks() + 1)
    }

    inline fun withSouls(stacks: Int): EncodedCell {
        if (stacks > MAX_STACKS) {
            throw IllegalArgumentException("Sould must be less or equal to $MAX_STACKS")
        }
        return EncodedCell(rawValue and SOULS_MASK.inv() or (stacks shl 4))
    }

    inline fun souls(): Int = (rawValue and SOULS_MASK) shr 4

    inline fun incrementSouls(): EncodedCell {
        if (souls() == MAX_STACKS) {
            return this
        }
        return withSouls(souls() + 1)
    }

    inline fun seedDifference(original: EncodedCell): Int? {
        if (rawValue < original.rawValue) {
            throw IllegalArgumentException("EncodedCell must be larger or equal to original")
        } else if (rawValue == original.rawValue) {
            return null
        }
        return rawValue - original.rawValue
    }
}

class PatronSaintsBlob :
    Blob(),
    CustomBlobCellEmission {
    init {
        // Act right after mobs, to give buffs to those who enter the area
        actPriority = MOB_PRIO - 1
    }

    fun stacksAt(pos: Int): Int = cur?.get(pos)?.let { EncodedCell(it).stacks() } ?: 0

    override fun evolve() {
        evolveUnchanged(off)

        var value: Int
        for (ch in chars()) {
            value = cur[ch.pos]
            if (value > 0) {
                if (ch.alignment != Char.Alignment.ALLY) {
                    Buff.affect(ch, PatronSaints::class.java)
                }
            }
        }
    }

    override fun seed(
        level: Level,
        cell: Int,
        amount: Int,
    ) {
        if (!level.insideMap(cell)) {
            return
        }

        val curCenter = cur?.get(cell)?.let { EncodedCell(it) } ?: EncodedCell.EMPTY

        if (amount == PATRON_SEED_SOUL) {
            val newCenter = curCenter.incrementStacks().incrementSouls()
            newCenter.seedDifference(curCenter)?.let { super.seed(level, cell, it) }

            for (o in PathFinder.NEIGHBOURS8) {
                if (level.insideMap(cell + o)) {
                    val pos = cell + o
                    val curNeighbour = cur?.get(pos)?.let { EncodedCell(it) } ?: EncodedCell.EMPTY
                    val newNeighbour = curNeighbour.incrementStacks()
                    newNeighbour.seedDifference(curNeighbour)?.let { super.seed(level, pos, it) }
                }
            }
        } else if (amount == PATRON_SEED_BLESS) {
            val newCenter = curCenter.withStacks(max(1, curCenter.stacks()))
            newCenter.seedDifference(curCenter)?.let { super.seed(level, cell, it) }
        } else if (DeviceCompat.isDebug()) {
            throw IllegalArgumentException("Invalid saint blob seed amount: $amount")
        }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(DamageReductionAreaFx.FACTORY, 0.1f)
    }

    override fun emit(
        emitter: BlobEmitter,
        factory: Emitter.Factory,
        index: Int,
        cellX: Int,
        cellY: Int,
        cell: Int,
        tileSize: Float,
    ) {
        var top = false
        var bottom = false
        var left = false
        var right = false

        for (idx in PathFinder.NEIGHBOURS4.indices) {
            val o = PathFinder.NEIGHBOURS4[idx]
            val nCur = cur[cell + o]
            if (o != 0) {
                if (nCur == 0) {
                    //  -width, -1, +1, +width
                    when (idx) {
                        0 -> top = true
                        1 -> left = true
                        2 -> right = true
                        3 -> bottom = true
                    }
                }
            }
        }

        val cur = EncodedCell(cur[cell])
        val time = Game.timeTotal

        val stacks = cur.stacks()
        val souls = cur.souls()

        if (souls > 0) {
            var t = time % (2 * PI.toFloat())
            val max = if (souls == 4 || souls == 8) souls + 3 else souls
            repeat(souls) {
                t += 2 * PI.toFloat() / max
                val fx =
                    DamageReductionAreaFx.emitOne(
                        emitter,
                        index,
                        (cellX + 0.5f + (cos(t) * sin(4 * t)) / 2) * tileSize,
                        (cellY + 0.5f + (sin(t) * sin(4 * t)) / 2) * tileSize,
                    )
                fx.color(0xFFA500)
                fx.acc.set(0f, 0f)
                fx.setSize(3f)
            }
        }

        repeat(2) {
            val k = Random.Int(4)
            val progress = Random.Float(1f)
            var ox = 0f
            var oy = 0f
            var emit = false
            if (k == 0 && top) {
                // top
                ox = progress
                oy = 0f
                emit = true
            } else if (k == 1 && right) {
                // right
                ox = 1f
                oy = progress
                emit = true
            } else if (k == 2 && bottom) {
                // bottom
                ox = 1 - progress
                oy = 1f
                emit = true
            } else if (k == 3 && left) {
                // left
                ox = 0f
                oy = 1 - progress
                emit = true
            }

            if (emit) {
                val shrunkTile = tileSize * 0.9f
                factory.emit(
                    emitter,
                    index,
                    cellX * tileSize + ox * shrunkTile,
                    cellY * tileSize + oy * shrunkTile,
                )
            }
        }
    }

    override fun tileDesc(): String = Messages.get(this, "desc")
}

class DamageReductionAreaFx : FlameParticle() {
    companion object {
        fun emitOne(
            emitter: Emitter,
            index: Int,
            x: Float,
            y: Float,
        ): DamageReductionAreaFx {
            val fx = (emitter.recycle(DamageReductionAreaFx::class.java) as DamageReductionAreaFx)
            fx.reset(x, y)
            fx.color(0x61780d)
            fx.acc.set(0f, -80f)
            fx.size = 3f
            return fx
        }

        val FACTORY: Emitter.Factory =
            object : Emitter.Factory() {
                override fun emit(
                    emitter: Emitter,
                    index: Int,
                    x: Float,
                    y: Float,
                ) {
                    emitOne(emitter, index, x, y)
                }

                override fun lightMode(): Boolean = true
            }
    }

    fun setSize(size: Float) {
        this.size = size
    }
}
