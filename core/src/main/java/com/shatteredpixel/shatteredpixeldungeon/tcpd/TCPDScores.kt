package com.shatteredpixel.shatteredpixeldungeon.tcpd

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.Rankings
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.getMap
import com.shatteredpixel.shatteredpixeldungeon.tcpd.ext.putMap
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.FileUtils
import java.io.IOException

class ModifierScore : Bundlable {
    var wins: Int = 0
        private set
    var losses: Int = 0
        private set

    fun copyWith(
        wins: Int = this.wins,
        losses: Int = this.losses,
    ): ModifierScore =
        ModifierScore().also {
            it.wins = wins
            it.losses = losses
        }

    override fun restoreFromBundle(bundle: Bundle) {
        wins = bundle.getInt(WINS)
        losses = bundle.getInt(LOSSES)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(WINS, wins)
        bundle.put(LOSSES, losses)
    }

    companion object {
        private const val WINS: String = "wins"
        private const val LOSSES: String = "losses"
    }
}

class TCPDScores : Bundlable {
    private var modifiers: MutableMap<Int, ModifierScore> = mutableMapOf()

    fun modifierScore(modifier: Modifier): ModifierScore = modifiers.getOrPut(modifier.id) { ModifierScore() }

    override fun restoreFromBundle(bundle: Bundle) {
        modifiers =
            bundle.getMap(
                MODIFIERS,
                { k -> getIntArray(k).toTypedArray() },
                { k -> getCollection(k).map { it as ModifierScore }.toTypedArray() },
            )
    }

    override fun storeInBundle(bundle: Bundle) {
        // remove empty modifiers
        for ((key, stats) in modifiers.toList()) {
            if (stats.wins == 0 && stats.losses == 0) {
                modifiers.remove(key)
            }
        }
        bundle.putMap(
            MODIFIERS,
            modifiers,
            { k, v -> put(k, v.toIntArray()) },
            { k, v -> put(k, v.toList()) },
        )
    }

    companion object {
        private var scores: TCPDScores? = null

        private const val MODIFIERS: String = "modifiers"
        private const val SCORES_FILE: String = "tcpd_scores.dat"

        fun submit(
            win: Boolean,
            record: Rankings.Record,
        ) {
            if (Dungeon.hero == null) {
                return
            }

            if (record.customSeed != null && record.customSeed.isNotEmpty()) {
                // Custom seed, do not record
                return
            }

            val scores = load()

            for (modifier in Modifier.ALL) {
                if (!modifier.active()) {
                    continue
                }
                val score = scores.modifierScore(modifier)
                scores.modifiers[modifier.id] =
                    score.copyWith(
                        wins = score.wins + if (win) 1 else 0,
                        losses = score.losses + if (win) 0 else 1,
                    )
            }

            save()
        }

        fun save() {
            val scores = scores ?: return
            synchronized(scores) {
                val bundle = Bundle()
                scores.storeInBundle(bundle)

                try {
                    FileUtils.bundleToFile(SCORES_FILE, bundle)
                } catch (e: IOException) {
                    ShatteredPixelDungeon.reportException(e)
                }
            }
        }

        fun load(): TCPDScores {
            if (scores != null) return scores!!
            val scores = TCPDScores()

            try {
                val bundle = FileUtils.bundleFromFile(SCORES_FILE)
                scores.restoreFromBundle(bundle)
            } catch (_: IOException) {
            }

            this.scores = scores
            return scores
        }
    }
}
