/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Feature;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

public abstract class TargetedClericSpell extends ClericSpell {

	@Override
	public void onCast(HolyTome tome, Hero hero ){
		if (Feature.SMART_TARGETING.enabled){
			GameScene.selectCell(new CellSelector.TargetedListener() {

				@Override
				protected void action(Char enemy) {
					onTargetSelected(tome, hero, enemy.pos);
				}

				@Override
				protected boolean isValidTarget(Char ch) {
					return validTarget(ch);
				}

				@Override
				protected boolean canAutoTarget(Char ch) {
					return validTarget(ch);
				}

				@Override
				protected void findTargets() {
					super.findTargets();
					targets.add(Dungeon.hero);
				}

				protected boolean canIgnore(Char ch) {
					switch (ch.alignment) {
						case ALLY: return true;
						case NEUTRAL: if(ch instanceof NPC) return true;
						case ENEMY: default:
							// this prevents potentially dangerous actions without forcing it every time.
							return ch.sprite != null && !ch.sprite.isVisible() && !validTarget(ch);
					}
				}

				@Override
				public String prompt() { return targetingPrompt(); }
			});
		} else {
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					onTargetSelected(tome, hero, cell);
				}

				@Override
				public String prompt() {
					return targetingPrompt();
				}
			});
		}
	}

	@Override
	public int targetingFlags(){
		return Ballistica.MAGIC_BOLT;
	}

	protected String targetingPrompt(){
		return Messages.get(this, "prompt");
	}

	protected boolean validTarget(Char ch){
		return ch != null && Dungeon.level.heroFOV[ch.pos];
	}

	protected abstract void onTargetSelected(HolyTome tome, Hero hero, Integer target);

}
