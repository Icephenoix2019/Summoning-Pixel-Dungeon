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

package com.trashboxbobylev.summoningpixeldungeon.levels.rooms.special;

import com.trashboxbobylev.summoningpixeldungeon.Challenges;
import com.trashboxbobylev.summoningpixeldungeon.Dungeon;
import com.trashboxbobylev.summoningpixeldungeon.items.Generator;
import com.trashboxbobylev.summoningpixeldungeon.items.Gold;
import com.trashboxbobylev.summoningpixeldungeon.items.Item;
import com.trashboxbobylev.summoningpixeldungeon.items.armor.Armor;
import com.trashboxbobylev.summoningpixeldungeon.items.keys.IronKey;
import com.trashboxbobylev.summoningpixeldungeon.items.weapon.Weapon;
import com.trashboxbobylev.summoningpixeldungeon.items.weapon.melee.staffs.Staff;
import com.trashboxbobylev.summoningpixeldungeon.levels.Level;
import com.trashboxbobylev.summoningpixeldungeon.levels.Terrain;
import com.trashboxbobylev.summoningpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

//import com.trashboxbobylev.shatteredpixeldungeon.actors.blobs.SacrificialFire;

public class AltarRoom extends SpecialRoom {

	public void paint( Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Dungeon.bossLevel( Dungeon.depth + 1 ) ? Terrain.HIGH_GRASS : Terrain.CHASM );

		Point c = center();
		Door door = entrance();
		if (door.x == left || door.x == right) {
			Point p = Painter.drawInside( level, this, door, Math.abs( door.x - c.x ) - 2, Terrain.EMPTY_SP );
			for (; p.y != c.y; p.y += p.y < c.y ? +1 : -1) {
				Painter.set( level, p, Terrain.EMPTY_SP );
			}
		} else {
			Point p = Painter.drawInside( level, this, door, Math.abs( door.y - c.y ) - 2, Terrain.EMPTY_SP );
			for (; p.x != c.x; p.x += p.x < c.x ? +1 : -1) {
				Painter.set( level, p, Terrain.EMPTY_SP );
			}
		}
		
		Painter.fill( level, c.x - 1, c.y - 1, 3, 3, Terrain.EMBERS );
		Painter.set( level, c, Terrain.PEDESTAL );
        level.drop( prize( level ), c.x + c.y * level.width());
		/*SacrificialFire fire = (SacrificialFire)level.blobs.get( SacrificialFire.class );
		if (fire == null) {
			fire = new SacrificialFire();
		}
		fire.seed( c.x + c.y * Level.WIDTH, 5 + Dungeon.depth * 5 );
		level.blobs.put( SacrificialFire.class, fire );*/

        entrance().set( Door.Type.LOCKED );
        level.addItemToSpawn( new IronKey( Dungeon.depth ) );
	}

    private static Item prize(Level level ) {

        //1 floor set higher than normal
        Staff prize = Generator.randomStaff( (Dungeon.depth / 5) + 1);

        //if it isn't already cursed, give it a free upgrade
        if (!prize.cursed){
            prize.upgrade();
            //curse the staff, unless it has a enchant
            if (!prize.hasGoodEnchant()){
                prize.enchant(Weapon.Enchantment.randomCurse());
            }
        }
        prize.cursed = prize.cursedKnown = true;

        return prize;
    }
}