package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.Char
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.tcpd.hooks.recalculateHT
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator
import com.watabou.noosa.Image
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

class Resizing :
    Buff(),
    AttackAmplificationBuff,
    TimescaleBuff,
    HtBoostBuff,
    DrRollBuff,
    DefSkillChangeBuff,
    PersistHeapNestingBuff {
    private var factor = 1f

    override fun attachTo(target: Char): Boolean {
        if (target.properties().contains(Char.Property.BOSS) ||
            target
                .properties()
                .contains(Char.Property.MINIBOSS)
        ) {
            return false
        }
        if (super.attachTo(target)) {
            target.needsShieldUpdate = true
            return true
        } else {
            return false
        }
    }

    override fun detach() {
        target.needsShieldUpdate = true
        super.detach()
    }

    fun multiply(factor: Float): Resizing {
        this.factor *= factor
        if (this.factor < MIN_THRESHOLD) {
            this.factor = MIN_THRESHOLD
        }
        target?.recalculateHT()
        return this
    }

    fun multiplyRandom() {
        multiply(Random.NormalFloat(MIN_THRESHOLD, MAX_THRESHOLD))
    }

    override fun attackMultiplier(): Float = forwardBuff()

    override fun speedFactor(): Float = backwardAsimptBuff()

    override fun htMultiplier(): Float {
        var b = forwardBuff()
        if (factor > 1.25f) b *= 1.25f
        return b
    }

    override fun drRollBonus(): Int = 0

    override fun verySketchyDrMultBonus(): Float = forwardBuff()

    override fun defRollMultiplier(attacker: Char): Float = backwardsToZeroBuff()

    override fun applyNestingEffect(target: Char) {
        affect(target, Resizing::class.java).multiply(factor)
    }

    // For stats that must get higher with size
    private fun forwardBuff(): Float {
        val f = max(factor, MIN_THRESHOLD)
        return if (f < MAX_THRESHOLD) {
            0.333777f * f.pow(3) + 0.66622293f
        } else {
            // straight out after 1.75
            f * 1.4f + 0.00506f
        }
    }

    // For stats that must get lower with size, and flat out to 0.5 at high size
    private fun backwardAsimptBuff(): Float {
        val f = max(factor, MIN_THRESHOLD)
        // naturally approaches 0.5
        return 0.491147f * f.pow(-2) + 0.5088528f
    }

    // For stats that must get lower with size, and reduce to 0 at high size
    private fun backwardsToZeroBuff(): Float =
        if (factor < 1f) {
            backwardAsimptBuff()
        } else {
            // decays to 0 evasion at scale 3
            max(1.5f - factor / 2, 0f)
        }

    override fun icon(): Int = BuffIndicator.UPGRADE

    override fun tintIcon(icon: Image) {
        icon.hardlight(0x00FFFF)
    }

    override fun desc(): String =
        Messages.get(
            this,
            "desc",
            ((factor) * 100f).roundToInt(),
            Messages.decimalFormat(
                "##.##",
                (attackMultiplier().toDouble() * 100).roundToInt() / 100.0,
            ),
            Messages.decimalFormat(
                "##.##",
                (verySketchyDrMultBonus().toDouble() * 100).roundToInt() / 100.0,
            ),
            Messages.decimalFormat(
                "##.##",
                (backwardsToZeroBuff().toDouble() * 100).roundToInt() / 100.0,
            ),
            Messages.decimalFormat("##.##", (htMultiplier().toDouble() * 100).roundToInt() / 100.0),
            Messages.decimalFormat("##.##", (speedFactor().toDouble() * 100).roundToInt() / 100.0),
        )

    override fun fx(on: Boolean) {
        if (on) {
            target.sprite.scale.set(GameMath.gate(MIN_THRESHOLD, factor, MAX_THRESHOLD))
            target.sprite.place(target.pos)
        } else {
            target.sprite.scale.set(1f)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(FACTOR, factor)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        factor = bundle.getFloat(FACTOR)
    }

    companion object {
        const val FACTOR = "factor"

        const val MIN_THRESHOLD = 0.5f
        const val MAX_THRESHOLD = 1.75f
    }
}
