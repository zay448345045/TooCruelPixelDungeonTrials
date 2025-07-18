/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tcpd.Modifier;

public class RatSkull extends Trinket {

	{
		image = ItemSpriteSheet.RAT_SKULL;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 8(14) -> 10(24) -> 12(36)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", (int)(exoticChanceMultiplier(buffedLvl())));
		} else {
			return Messages.get(this, "typical_stats_desc", (int)(exoticChanceMultiplier(0)));
		}
	}

	public static float exoticChanceMultiplier(){
		float chance = exoticChanceMultiplier(trinketLevel(RatSkull.class));
		if(Modifier.EVOLUTION.active()) chance = 50f/4f;
		if(Modifier.MUTAGEN.active()) chance = 1e6f;
		return chance;
	}

	public static float exoticChanceMultiplier( int level ){
		if (level == -1){
			return 1f;
		} else {
			return 2f + 1f*level;
		}
	}

}
