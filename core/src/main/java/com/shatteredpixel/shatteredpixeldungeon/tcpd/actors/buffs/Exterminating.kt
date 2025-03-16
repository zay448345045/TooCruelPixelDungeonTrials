package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.tcpd.effects.ExterminationIndicator

class Exterminating : Buff() {
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

    class Reveal : FlavourBuff(), MindVisionExtBuff {
        companion object {
            val DURATION = TICK * 2f
        }

        override fun detach() {
            FullSceneUpdater.requestFog()
            super.detach()
        }

        override fun revealRadius(): Int {
            return 1
        }
    }
}