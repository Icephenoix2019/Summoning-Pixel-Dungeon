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

package com.trashboxbobylev.summoningpixeldungeon.items.bombs;

import com.trashboxbobylev.summoningpixeldungeon.Assets;
import com.trashboxbobylev.summoningpixeldungeon.Dungeon;
import com.trashboxbobylev.summoningpixeldungeon.actors.Actor;
import com.trashboxbobylev.summoningpixeldungeon.actors.Char;
import com.trashboxbobylev.summoningpixeldungeon.actors.buffs.Buff;
import com.trashboxbobylev.summoningpixeldungeon.actors.mobs.Mimic;
import com.trashboxbobylev.summoningpixeldungeon.actors.mobs.Mob;
import com.trashboxbobylev.summoningpixeldungeon.effects.CellEmitter;
import com.trashboxbobylev.summoningpixeldungeon.effects.Speck;
import com.trashboxbobylev.summoningpixeldungeon.items.Heap;
import com.trashboxbobylev.summoningpixeldungeon.items.Item;
import com.trashboxbobylev.summoningpixeldungeon.items.wands.WandOfBlastWave;
import com.trashboxbobylev.summoningpixeldungeon.mechanics.Ballistica;
import com.trashboxbobylev.summoningpixeldungeon.sprites.ItemSpriteSheet;
import com.trashboxbobylev.summoningpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class Noisemaker extends Bomb {
	
	{
		image = ItemSpriteSheet.NOISEMAKER;
		fuseDelay = 20;
	}

    @Override
    public void explode(int cell) {
        super.explode(cell);
        PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 3 );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char ch = Actor.findChar(i);
                if (ch != null) {
                    //trace a ballistica to our target (which will also extend past them
                    Ballistica trajectory = new Ballistica(ch.pos, cell, Ballistica.PROJECTILE);
                    //trim it to just be the part that goes past them
                    trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
                    //knock them back along that ballistica
                    WandOfBlastWave.throwChar(ch, trajectory, 3);
                }
            }
        }
    }

//    public void setTrigger(int cell){
//
//		Buff.affect(Dungeon.hero, Trigger.class).set(cell);
//
//		CellEmitter.center( cell ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
//		Sample.INSTANCE.play( Assets.SND_ALERT );
//
//		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
//			mob.beckon( cell );
//		}
//
//
//
//	}
	
	public static class Trigger extends Buff {

		int cell;
		int floor;
		int left;
		
		public void set(int cell){
			floor = Dungeon.depth;
			this.cell = cell;
			left = 6;
		}
		
		@Override
		public boolean act() {

			if (Dungeon.depth != floor){
				spend(TICK);
				return true;
			}

			Noisemaker bomb = null;
			Heap heap = Dungeon.level.heaps.get(cell);

			if (heap != null){
				for (Item i : heap.items){
					if (i instanceof Noisemaker){
						bomb = (Noisemaker) i;
						break;
					}
				}
			}

			if (bomb == null) {
				detach();

			}  else {
				spend(TICK);

				left--;

				if (left <= 0){
					CellEmitter.center( cell ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
					Sample.INSTANCE.play( Assets.SND_ALERT );

					for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
						mob.beckon( cell );
					}
                    for (Heap levelheap : Dungeon.level.heaps.valueList()) {
                        if (levelheap.type == Heap.Type.MIMIC) {
                            Mimic m = Mimic.spawnAt( levelheap.pos, levelheap.items );
                            if (m != null) {
                                m.beckon( cell );
                                levelheap.destroy();
                            }
                        }
                    }
					left = 4;
				}

			}

			return true;
		}

		private static final String CELL = "cell";
		private static final String FLOOR = "floor";
		private static final String LEFT = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CELL, cell);
			bundle.put(FLOOR, floor);
			bundle.put(LEFT, left);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			cell = bundle.getInt(CELL);
			floor = bundle.getInt(FLOOR);
			left = bundle.getInt(LEFT);
		}
	}
	
	@Override
	public int price() {
		//prices of ingredients
		return quantity * (35 + 50);
	}
}
