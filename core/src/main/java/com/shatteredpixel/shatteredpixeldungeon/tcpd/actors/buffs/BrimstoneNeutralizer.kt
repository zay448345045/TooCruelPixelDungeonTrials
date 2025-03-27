package com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.buffs

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone
import com.shatteredpixel.shatteredpixeldungeon.tcpd.actors.blobs.findBlob
import com.watabou.utils.PathFinder

class BrimstoneNeutralizer : Buff() {
    override fun act(): Boolean {
        val fireBlob = Dungeon.level.findBlob<Fire>()
        val t = target
        if (fireBlob != null && fireBlob.cur[t.pos] > 0 && t.glyphLevel(Brimstone::class.java) >= 0) {
            when (t) {
                is Hero -> {
                    ScorchedEarth.explode(t, t.belongings.armor())
                }

                is ArmoredStatue -> {
                    ScorchedEarth.explode(t, t.armor())
                }

                else -> {
                    ScorchedEarth.explode(t, null)
                }
            }
            for (o in PathFinder.NEIGHBOURS9) {
                fireBlob.clear(t.pos + o)
            }
        }
        return super.act()
    }
}
