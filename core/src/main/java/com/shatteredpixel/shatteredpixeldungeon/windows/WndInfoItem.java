/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.abilities.Ability;
import com.shatteredpixel.shatteredpixeldungeon.items.magic.ConjurerSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.staffs.Staff;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndInfoItem extends Window {
	
	private static final float GAP	= 2;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	
	public WndInfoItem( Heap heap ) {
		
		super();

		if (heap.type == Heap.Type.HEAP) {
			fillFields( heap.peek() );

		} else {
			fillFields( heap );

		}
	}
	
	public WndInfoItem( Item item ) {
		super();
		
		fillFields( item );
	}
	
	private void fillFields( Heap heap ) {
		
		IconTitle titlebar = new IconTitle( heap );
		titlebar.color( TITLE_COLOR );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( heap.info(), 6 );

		layoutFields(titlebar, txtInfo);
	}
	
	private void fillFields( Item item ) {
		
		int color = TITLE_COLOR;
		if (!(item instanceof Staff || item instanceof ConjurerSpell || item instanceof Wand || item instanceof Ability)) {
			if (item.levelKnown && item.level() > 0) {
				color = ItemSlot.UPGRADED;
			} else if (item.levelKnown && item.level() < 0) {
				color = ItemSlot.DEGRADED;
			}
		} else {
			switch (item.level()){
				case 0: color = ItemSlot.BRONZE; break;
				case 1: color = ItemSlot.SILVER; break;
				case 2: color = ItemSlot.GOLD; break;
			}
		}

		IconTitle titlebar = new IconTitle( item );
		titlebar.color( color );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( item.info(), 6 );
		
		layoutFields(titlebar, txtInfo);
	}

	private void layoutFields(IconTitle title, RenderedTextBlock info){
		int width = WIDTH_MIN;

		info.maxWidth(width);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width);
		}

		title.setRect( 0, 0, width, 0 );
		add( title );

		info.setPos(title.left(), title.bottom() + GAP);
		add( info );

		resize( width, (int)(info.bottom() + 2) );
	}
}
