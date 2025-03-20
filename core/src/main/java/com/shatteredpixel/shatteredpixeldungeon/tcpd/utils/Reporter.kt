package com.shatteredpixel.shatteredpixeldungeon.tcpd.utils

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog

fun reportRecoverableError(text: String) {
    GLog.n("!!!INTERNAL ERROR!!!: $text")
    GLog.n(
        "Please report this to the developer. Modifiers: ${Dungeon.tcpdData.modifiers.serializeToString()} Seed: ${
            DungeonSeed.convertToCode(
                Dungeon.seed,
            )}",
    )
}
