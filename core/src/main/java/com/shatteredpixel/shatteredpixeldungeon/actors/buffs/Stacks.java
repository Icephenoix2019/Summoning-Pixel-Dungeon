/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SacrificialParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Stacks extends Buff {

	public float damage = 0;

	private static final String DAMAGE	= "damage";

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DAMAGE, damage );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		damage = bundle.getFloat( DAMAGE );
	}

	public void add(int stack) {
		if (this.damage < 30) damage = GameMath.gate(0, damage + stack + 1, 31);
		if (damage == 11 || damage == 21 || damage == 31) {
			target.sprite.emitter().burst(SacrificialParticle.FACTORY, 15);
			Sample.INSTANCE.play(Assets.Sounds.BURNING);
		}
	}
	
	@Override
	public int icon() {
		return BuffIndicator.SACRIFICE;
	}
	
	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.5f, 0f);
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (30 - damage) / 30);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)damage);
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {
			damage--;
			if (damage < 0){
				detach();
			}
			spend(TICK);
		} else {
			detach();
		}

		return true;
	}

}
