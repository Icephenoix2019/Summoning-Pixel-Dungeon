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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.minions;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.PerfumeGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.powers.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.minions.stationary.StationaryMinion;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfTargeting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public abstract class Minion extends Mob {

    public int independenceRange = 8;

    public enum  MinionClass{
        DEFENSE, MELEE, MAGIC, RANGE, SUPPORT
    }

    public MinionClass minionClass = MinionClass.MELEE;

    public int minDamage = 0;
    public int maxDamage = 0;

    protected int minDR = 0;
    protected int maxDR = 0;
    public int baseMinDR = 0;
    public int baseMaxDR = 0;

    public int strength = 9;
    public int defendingPos = -1;

    public boolean isTanky = false;
    public Weapon.Enchantment enchantment;
    public int lvl;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("minDamage", minDamage);
        bundle.put("maxDamage", maxDamage);
        bundle.put("minDR", minDR);
        bundle.put("maxDR", maxDR);
        bundle.put("str", strength);
        bundle.put("enchantment", enchantment);
        bundle.put("level", lvl);
        bundle.put("class", minionClass);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        minDamage = bundle.getInt("minDamage");
        maxDamage = bundle.getInt("maxDamage");
        minDR = bundle.getInt("minDR");
        maxDR = bundle.getInt("maxDR");
        strength = bundle.getInt("str");
        lvl = bundle.getInt("lvl");
        enchantment = (Weapon.Enchantment) bundle.get("enchantment");
        minionClass = bundle.getEnum("class", MinionClass.class);
    }

    public float attunement = 1;

    {
        //all minions are allies and kinda intelligent
        alignment = Alignment.ALLY;
        intelligentAlly = true;

        WANDERING = new Wandering();
        state = WANDERING;

        //before other mobs
        actPriority = MOB_PRIO + 1;

        immunities.add(PerfumeGas.Affection.class);
    }

    @Override
    public String getName() {
        return enchantment != null ? enchantment.name( super.getName() ) : super.getName();
    }

    public void setMaxHP(int hp){
        HP = HT = hp;
    }

    public void setStrength(int str){
        strength = str;
    }

    public void setDamage(int min, int max){
        minDamage = min;
        maxDamage = max;
    }

    public void adjustDamage(int min, int max){
        minDamage += min;
        maxDamage += max;
    }

    @Override
    public int damageRoll() {
        int i = Random.NormalIntRange(minDamage, maxDamage);
        if (buff(AdditionalDamage.class) != null) i += minDamage*2;
        if (Dungeon.hero.buff(Attunement.class) != null) i *= Attunement.empowering();
        return i;
    }

    public void setDR(int min, int max){
        minDR = min;
        maxDR = max;
    }

    @Override
    public void damage(int dmg, Object src) {
        if (AntiMagic.RESISTS.contains(src.getClass()) && buff(MagicalResistance.class) != null){
            dmg -= Random.NormalIntRange(0, 7);
            if (dmg < 0) dmg = 0;
        }
        super.damage(dmg, src);
    }

    public void adjustDR(int min, int max){
        minDR += Math.max(-2, min);
        maxDR += Math.max(-4, max);
    }

    @Override
    public int drRoll() {
        int i = Random.NormalIntRange(minDR + baseMinDR, maxDR + baseMaxDR);
        if (buff(AdditionalDefense.class) != null) i += Random.NormalIntRange(1, 5);
        return i;
    }

    @Override
    public Char chooseEnemy() {
        Char enemy = super.chooseEnemy();

        int targetPos = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
        int distance = this instanceof StationaryMinion ? Integer.MAX_VALUE : independenceRange;



        //will never attack something far from their target
        if (enemy != null
                && Dungeon.level.mobs.contains(enemy)
                && (Dungeon.level.distance(enemy.pos, targetPos) <= distance)
                && (invisible == 0)){
            if (!(enemy instanceof Piranha && enemy.HP != enemy.HT))
            return enemy;
        }

        return null;
    }

    //same accuracy and dexterity as player
    @Override
    public int defenseSkill(Char enemy) {
        boolean seen = (enemySeen && enemy.invisible == 0);

        if (enemy == Dungeon.hero && !Dungeon.hero.canSurpriseAttack()) seen = true;
        if ( seen
                && paralysed == 0
                && !(alignment == Alignment.ALLY && enemy == Dungeon.hero)) {
            int i = Dungeon.hero.defenseSkill(enemy);
            if (buff(AdditionalEvasion.class) != null) i *= 1.3f;
            return i;
        } else {
            return 0;
        }
    }

    @Override
    public void add(Buff buff) {
        super.add(buff);
        if (buff instanceof TankHeal && minionClass == MinionClass.DEFENSE){
            HP = HT;
            sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
            buff.detach();
        }
    }

    @Override
    public int attackSkill(Char target) {

        int encumbrance = strength - Dungeon.hero.STR();

        float accuracy = 1;
        accuracy *= RingOfAccuracy.accuracyMultiplier( Dungeon.hero );

        if (encumbrance > 0){
            accuracy /= Math.pow(1.5, encumbrance);
        }

        return (int) (Dungeon.hero.getAttackSkill() * accuracy);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (enchantment != null && buff(MagicImmune.class) == null) {
            damage = enchantment.proc(  this, enemy, damage );
        }
        if (buff(RootingOnShots.class) != null && Random.Float() < 0.5) Buff.prolong(enemy, Roots.class, 5f);
        return super.attackProc(enemy, damage);
    }



    //ported from DriedRose.java
    //minions will always move towards hero if enemies not here
    private class Wandering extends Mob.Wandering {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            StoneOfTargeting.Defending defending = buff(StoneOfTargeting.Defending.class);
            if ( enemyInFOV && defending == null ) {

                enemySeen = true;

                notice();
                alerted = true;
                state = HUNTING;
                target = enemy.pos;

            } else {

                enemySeen = false;
                if (defending != null) {
                    defendingPos = defending.position;
                } else defendingPos = -1;

                int oldPos = pos;
                target = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
                //always move towards the hero when wandering
                if (getCloser( target )) {
                    //moves 2 tiles at a time when returning to the hero
                    if (defendingPos == -1 && !Dungeon.level.adjacent(target, pos)){
                        getCloser( target );
                    }
                    spend( 1 / speed() );
                    return moveSprite( oldPos, pos );
                } else {
                    spend( TICK );
                }

            }
            return true;
        }

    }

    @Override
    public String description() {
        String d = super.description();
        return d + "\n\n" + Messages.get(Minion.class, "stats", minDamage, maxDamage, HP, HT, minDR + baseMinDR, maxDR + baseMaxDR);
    }

    public void destroy() {
        Dungeon.hero.usedAttunement -= attunement;
        super.destroy();
    }

    public void onLeaving(){}
}
