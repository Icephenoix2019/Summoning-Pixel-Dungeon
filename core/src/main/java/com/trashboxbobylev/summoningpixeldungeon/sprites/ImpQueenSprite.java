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

package com.trashboxbobylev.summoningpixeldungeon.sprites;

import com.trashboxbobylev.summoningpixeldungeon.Assets;
import com.trashboxbobylev.summoningpixeldungeon.actors.mobs.minions.ImpQueen;
import com.trashboxbobylev.summoningpixeldungeon.effects.CellEmitter;
import com.trashboxbobylev.summoningpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class ImpQueenSprite extends MobSprite {

	public ImpQueenSprite() {
		super();
		
		texture( Assets.IMP_QUEEN );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 6, 6, 6, 7, 6, 6, 7, 7 );
		
		run = new Animation( 4, true );
		run.frames( frames, 7, 8 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 9, 10, 11, 12 );
		
		zap = new Animation( 8, false );
        zap.frames( frames, 13, 14, 15, 16, 17, 18, 19, 20, 9 );
		
		die = new Animation( 15, false );
		die.frames( frames, 21, 22, 23, 24, 25 );
		
		play( idle );
	}
	
	public void zap( int cell ) {
		
		turnTo( ch.pos , cell );
		play( zap );

		MagicMissile.boltFromChar( parent,
				MagicMissile.WARD,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						((ImpQueen)ch).onZapComplete();
					}
				} );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
            CellEmitter.center(ch.pos).burst(MagicMissile.WardParticle.FACTORY, Random.NormalIntRange(12, 20));
		}
		super.onComplete( anim );
	}
}
