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

package com.trashboxbobylev.summoningpixeldungeon.items.scrolls.exotic;

import com.trashboxbobylev.summoningpixeldungeon.Assets;
import com.trashboxbobylev.summoningpixeldungeon.ShatteredPixelDungeon;
import com.trashboxbobylev.summoningpixeldungeon.actors.buffs.Invisibility;
import com.trashboxbobylev.summoningpixeldungeon.effects.Identification;
import com.trashboxbobylev.summoningpixeldungeon.items.Item;
import com.trashboxbobylev.summoningpixeldungeon.items.potions.Potion;
import com.trashboxbobylev.summoningpixeldungeon.items.rings.Ring;
import com.trashboxbobylev.summoningpixeldungeon.items.scrolls.Scroll;
import com.trashboxbobylev.summoningpixeldungeon.messages.Messages;
import com.trashboxbobylev.summoningpixeldungeon.scenes.GameScene;
import com.trashboxbobylev.summoningpixeldungeon.scenes.PixelScene;
import com.trashboxbobylev.summoningpixeldungeon.sprites.ItemSprite;
import com.trashboxbobylev.summoningpixeldungeon.ui.RenderedTextMultiline;
import com.trashboxbobylev.summoningpixeldungeon.ui.Window;
import com.trashboxbobylev.summoningpixeldungeon.utils.GLog;
import com.trashboxbobylev.summoningpixeldungeon.windows.IconTitle;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class ScrollOfDivination extends ExoticScroll {
	
	{
		initials = 0;
	}
	
	@Override
	public void doRead() {
		
		curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );
		
		readAnimation();
		setKnown();
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel();
		
		HashSet<Class<? extends Potion>> potions = Potion.getUnknown();
		HashSet<Class<? extends Scroll>> scrolls = Scroll.getUnknown();
		HashSet<Class<? extends Ring>> rings = Ring.getUnknown();
		
		int total = potions.size() + scrolls.size() + rings.size();
		
		if (total == 0){
			GLog.negative( Messages.get(this, "nothing_left") );
			return;
		}
		
		ArrayList<Item> IDed = new ArrayList<>();
		int left = 4;
		
		float[] baseProbs = new float[]{3, 3, 3};
		float[] probs = baseProbs.clone();
		
		while (left > 0 && total > 0) {
			try {
				switch (Random.chances(probs)) {
					default:
						probs = baseProbs.clone();
						continue;
					case 0:
						if (potions.isEmpty()) {
							probs[0] = 0;
							continue;
						}
						probs[0]--;
						Potion p = Random.element(potions).newInstance();
						p.setKnown();
						IDed.add(p);
						potions.remove(p.getClass());
						break;
					case 1:
						if (scrolls.isEmpty()) {
							probs[1] = 0;
							continue;
						}
						probs[1]--;
						Scroll s = Random.element(scrolls).newInstance();
						s.setKnown();
						IDed.add(s);
						scrolls.remove(s.getClass());
						break;
					case 2:
						if (rings.isEmpty()) {
							probs[2] = 0;
							continue;
						}
						probs[2]--;
						Ring r = Random.element(rings).newInstance();
						r.setKnown();
						IDed.add(r);
						rings.remove(r.getClass());
						break;
				}
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException(e);
			}
			left --;
			total --;
		}
		
		GameScene.show(new WndDivination( IDed ));
	}
	
	private class WndDivination extends Window {
		
		private static final int WIDTH = 120;
		
		WndDivination(ArrayList<Item> IDed ){
			IconTitle cur = new IconTitle(new ItemSprite(ScrollOfDivination.this),
					Messages.titleCase(Messages.get(ScrollOfDivination.class, "name")));
			cur.setRect(0, 0, WIDTH, 0);
			add(cur);
			
			RenderedTextMultiline msg = PixelScene.renderMultiline(Messages.get(this, "desc"), 6);
			msg.maxWidth(120);
			msg.setPos(0, cur.bottom() + 2);
			add(msg);
			
			float pos = msg.bottom() + 10;
			
			for (Item i : IDed){
				
				cur = new IconTitle(i);
				cur.setRect(0, pos, WIDTH, 0);
				add(cur);
				pos = cur.bottom() + 2;
				
			}
			
			resize(WIDTH, (int)pos);
		}
		
	}
}
