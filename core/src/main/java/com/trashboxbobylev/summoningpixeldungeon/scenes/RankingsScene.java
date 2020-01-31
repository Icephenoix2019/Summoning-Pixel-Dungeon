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

package com.trashboxbobylev.summoningpixeldungeon.scenes;

import com.trashboxbobylev.summoningpixeldungeon.Assets;
import com.trashboxbobylev.summoningpixeldungeon.Rankings;
import com.trashboxbobylev.summoningpixeldungeon.ShatteredPixelDungeon;
import com.trashboxbobylev.summoningpixeldungeon.actors.hero.HeroClass;
import com.trashboxbobylev.summoningpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.trashboxbobylev.summoningpixeldungeon.effects.Flare;
import com.trashboxbobylev.summoningpixeldungeon.items.weapon.melee.Sword;
import com.trashboxbobylev.summoningpixeldungeon.messages.Messages;
import com.trashboxbobylev.summoningpixeldungeon.sprites.ItemSprite;
import com.trashboxbobylev.summoningpixeldungeon.sprites.ItemSpriteSheet;
import com.trashboxbobylev.summoningpixeldungeon.ui.*;
import com.trashboxbobylev.summoningpixeldungeon.ui.changelist.ChangeButton;
import com.trashboxbobylev.summoningpixeldungeon.ui.changelist.ChangesWindow;
import com.trashboxbobylev.summoningpixeldungeon.windows.WndError;
import com.trashboxbobylev.summoningpixeldungeon.windows.WndRanking;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.GameMath;

public class RankingsScene extends PixelScene {
	
	private static final float ROW_HEIGHT_MAX	= 20;
	private static final float ROW_HEIGHT_MIN	= 12;

	private static final float MAX_ROW_WIDTH    = 160;

	private static final float GAP	= 4;
	
	private Archs archs;

	@Override
	public void create() {
		
		super.create();
		
		Music.INSTANCE.play( Assets.THEME, true );

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		archs = new Archs();
		archs.setSize( w, h );
		add( archs );
		
		Rankings.INSTANCE.load();

		RenderedText title = PixelScene.renderText( Messages.get(this, "title"), 9);
		title.hardlight(Window.TITLE_COLOR);
		title.x = (w - title.width()) / 2f;
		title.y = (20 - title.baseLine()) / 2f;

		align(title);
		add(title);
		
		if (Rankings.INSTANCE.records.size() > 0) {

			//attempts to give each record as much space as possible, ideally as much space as portrait mode
			float rowHeight = GameMath.gate(ROW_HEIGHT_MIN, (uiCamera.height - 26)/Rankings.INSTANCE.records.size(), ROW_HEIGHT_MAX);

			float left = (w - Math.min( MAX_ROW_WIDTH, w )) / 2 + GAP;
			float top = (h - rowHeight  * Rankings.INSTANCE.records.size()) / 2;
			
			int pos = 0;
			
			for (Rankings.Record rec : Rankings.INSTANCE.records) {
				Record row = new Record( pos, pos == Rankings.INSTANCE.lastRecord, rec );
				float offset =
						rowHeight <= 14 ?
								pos %2 == 1?
										5 :
										-5
								: 0;
				row.setRect( left+offset, top + pos * rowHeight, w - left * 2, rowHeight );
				add(row);
				
				pos++;
			}
			
			if (Rankings.INSTANCE.totalNumber >= Rankings.TABLE_SIZE) {
				RenderedText label = PixelScene.renderText( Messages.get(this, "total") + " ", 8 );
				label.hardlight( 0xCCCCCC );
				add( label );

				RenderedText won = PixelScene.renderText( Integer.toString( Rankings.INSTANCE.wonNumber ), 8 );
				won.hardlight( Window.SHPX_COLOR );
				add( won );

				RenderedText total = PixelScene.renderText( "/" + Rankings.INSTANCE.totalNumber, 8 );
				total.hardlight( 0xCCCCCC );
				total.x = (w - total.width()) / 2;
				total.y = top + pos * rowHeight + GAP;
				add( total );

				float tw = label.width() + won.width() + total.width();
				label.x = (w - tw) / 2;
				won.x = label.x + label.width();
				total.x = won.x + won.width();
				label.y = won.y = total.y = h - label.height() - GAP;

				align(label);
				align(total);
				align(won);

			}


            RedButton btnInfo = new RedButton( "?" ) {
                @Override
                protected void onClick() {
                    ShatteredPixelDungeon.scene().add(new ChangesWindow(Icons.INFO.get(), Messages.get(RankingsScene.class, "score"), Messages.get(RankingsScene.class, "score_info")));
                }
            };
            btnInfo.enable( true );
            btnInfo.setRect( 10, 5, 20, 15 );
            add( btnInfo );

        } else {

			RenderedText noRec = PixelScene.renderText(Messages.get(this, "no_games"), 8);
			noRec.hardlight( 0xCCCCCC );
			noRec.x = (w - noRec.width()) / 2;
			noRec.y = (h - noRec.height()) / 2;
			align(noRec);
			add(noRec);
			
		}

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );


		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(TitleScene.class);
	}
	
	public static class Record extends Button {
		
		private static final float GAP	= 4;
		
		private static final int[] TEXT_WIN	= {0xFFFF88, 0xB2B25F};
		private static final int[] TEXT_LOSE= {0xDDDDDD, 0x888888};
		private static final int FLARE_WIN	= 0x888866;
		private static final int FLARE_LOSE	= 0x666666;
		
		private Rankings.Record rec;
		
		protected ItemSprite shield;
		private Flare flare;
		private BitmapText position;
		private RenderedTextMultiline desc;
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;
		
		public Record( int pos, boolean latest, Rankings.Record rec ) {
			super();
			
			this.rec = rec;
			
			if (latest) {
				flare = new Flare( 6, 24 );
				flare.angularSpeed = 90;
				flare.color( rec.win ? FLARE_WIN : FLARE_LOSE );
				addToBack( flare );
			}

			if (pos != Rankings.TABLE_SIZE-1) {
				position.text(Integer.toString(pos + 1));
			} else
				position.text(" ");
			position.measure();
			
			desc.text( Messages.titleCase(rec.desc()) );

			//desc.measure();

			int odd = pos % 2;
			
			if (rec.win) {
			    if (rec.cause == Sword.class) shield.view( ItemSpriteSheet.BONES, null);
				else shield.view( ItemSpriteSheet.AMULET, null );
				position.hardlight( TEXT_WIN[odd] );
				desc.hardlight( TEXT_WIN[odd] );
				depth.hardlight( TEXT_WIN[odd] );
				level.hardlight( TEXT_WIN[odd] );
			} else {
				position.hardlight( TEXT_LOSE[odd] );
				desc.hardlight( TEXT_LOSE[odd] );
				depth.hardlight( TEXT_LOSE[odd] );
				level.hardlight( TEXT_LOSE[odd] );

				if (rec.depth != 0){
					depth.text( Integer.toString(rec.depth) );
					depth.measure();
					steps.copy(Icons.DEPTH.get());

					add(steps);
					add(depth);
				}

			}

			if (rec.herolevel != 0){
				level.text( Integer.toString(rec.herolevel) );
				level.measure();
				add(level);
			}
			
			classIcon.copy( Icons.get( rec.heroClass ) );
			if (rec.heroClass == HeroClass.ROGUE){
				//cloak of shadows needs to be brightened a bit
				classIcon.brightness(2f);
			}
		}
		
		@Override
		protected void createChildren() {
			
			super.createChildren();
			
			shield = new ItemSprite( ItemSpriteSheet.TOMB, null );
			add( shield );
			
			position = new BitmapText( PixelScene.pixelFont);
			add( position );
			
			desc = renderMultiline( 7 );
			add( desc );

			depth = new BitmapText( PixelScene.pixelFont);

			steps = new Image();
			
			classIcon = new Image();
			add( classIcon );

			level = new BitmapText( PixelScene.pixelFont);
		}
		
		@Override
		protected void layout() {
			
			super.layout();
			
			shield.x = x;
			shield.y = y + (height - shield.height) / 2f;
			align(shield);
			
			position.x = shield.x + (shield.width - position.width()) / 2f;
			position.y = shield.y + (shield.height - position.height()) / 2f + 1;
			align(position);
			
			if (flare != null) {
				flare.point( shield.center() );
			}

			classIcon.x = x + width - 16 + (16 - classIcon.width())/2f;
			classIcon.y = shield.y + (16 - classIcon.height())/2f;
			align(classIcon);

			level.x = classIcon.x + (classIcon.width - level.width()) / 2f;
			level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1;
			align(level);

			steps.x = x + width - 32 + (16 - steps.width())/2f;
			steps.y = shield.y + (16 - steps.height())/2f;
			align(steps);

			depth.x = steps.x + (steps.width - depth.width()) / 2f;
			depth.y = steps.y + (steps.height - depth.height()) / 2f + 1;
			align(depth);

			desc.maxWidth((int)(steps.x - (shield.x + shield.width + GAP)));
			desc.setPos(shield.x + shield.width + GAP, shield.y + (shield.height - desc.height()) / 2f + 1);
			align(desc);
		}
		
		@Override
		protected void onClick() {
			if (rec.gameData != null) {
				parent.add( new WndRanking( rec ) );
			} else {
				parent.add( new WndError( Messages.get(RankingsScene.class, "no_info") ) );
			}
		}
	}
}
