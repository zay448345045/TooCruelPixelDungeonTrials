package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.PatronSaintsBlob
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.findBlob
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.noosa.Image
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class PatronSaints : Buff(), AttackAmplificationBuff, DamageAmplificationBuff {
    var lastShownStacks = 0;

    override fun icon(): Int {
        return BuffIndicator.LIGHT
    }

    override fun act(): Boolean {
        val stacks = stacks()
        if (stacks == 0) {
            detach()
            return true
        } else {
            if (stacks != lastShownStacks) {
                fx(true)
            }
        }
        spend(TICK)
        return true
    }

    override fun tintIcon(icon: Image) {
        icon.hardlight(0xFFA500)
    }

    fun stacks(): Int {
        return findBlob<PatronSaintsBlob>(Dungeon.level)?.stacksAt(target.pos) ?: 0
    }

    override fun damageMultiplier(source: Any?): Float {
        return 1 - min(sqrt(stacks() / 9.8765f), 0.90f)
    }

    override fun attackMultiplier(): Float {
        return 1.3f.pow(stacks())
    }

    override fun fx(on: Boolean) {
        if (on) {
            lastShownStacks = stacks()
            target.sprite.customAura(
                PatronSaints::class.java,
                0xFFA500,
                0.5f + (1.5f * lastShownStacks) / 8,
                true
            )
        } else {
            target.sprite.clearCustomAura(PatronSaints::class.java)
        }
    }

    override fun desc(): String {
        return Messages.get(
            this,
            "desc",
            stacks(),
            Messages.decimalFormat("##", (attackMultiplier() - 1) * 100.0),
            Messages.decimalFormat("##", (1 - damageMultiplier(null)) * 100.0),
        )
    }
}