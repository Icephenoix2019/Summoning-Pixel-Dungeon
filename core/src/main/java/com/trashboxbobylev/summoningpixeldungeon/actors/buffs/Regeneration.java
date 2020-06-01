/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2020 TrashboxBobylev
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

package com.trashboxbobylev.summoningpixeldungeon.actors.buffs;

import com.trashboxbobylev.summoningpixeldungeon.Dungeon;
import com.trashboxbobylev.summoningpixeldungeon.actors.hero.Hero;
import com.trashboxbobylev.summoningpixeldungeon.actors.hero.HeroSubClass;
import com.trashboxbobylev.summoningpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.trashboxbobylev.summoningpixeldungeon.items.artifacts.LoveHolder;

public class Regeneration extends Buff {
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
	}
	
	private static final float REGENERATION_DELAY = 16;
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			if (target.HP < regencap() && !((Hero)target).isStarving() && ((Hero) target).subClass != HeroSubClass.OCCULTIST) {
				LockedFloor lock = target.buff(LockedFloor.class);
				if (target.HP > 0 && (lock == null || lock.regenOn())) {
					target.HP += 1;
					if (target.HP == regencap()) {
						((Hero) target).resting = false;
					}
				}
			}

            LoveHolder.lul lul = target.buff(LoveHolder.lul.class);

			ChaliceOfBlood.chaliceRegen regenBuff = Dungeon.hero.buff( ChaliceOfBlood.chaliceRegen.class);

			float tick = REGENERATION_DELAY;
			if (Dungeon.depth == 21) tick /= 10;

			if (regenBuff != null)
				if (regenBuff.isCursed())
					tick *= 1.5f;
				else
					tick -= regenBuff.itemLevel()*0.9f;

				spend( tick );
			
		} else {
			
			diactivate();
			
		}
		
		return true;
	}
	
	public int regencap(){
		return target.HT;
	}
}
