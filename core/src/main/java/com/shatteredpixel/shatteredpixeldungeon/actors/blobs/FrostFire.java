/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Community Pixel Dungeon
 * Copyright (C) 2024-2025 Trashbox Bobylev and Pixel Dungeon's community
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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frostburn;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

public class FrostFire extends Blob {

    @Override
    protected void evolve() {

        boolean[] flamable = Dungeon.level.flamable;
        int cell;

        Freezing freeze = (Freezing)Dungeon.level.blobs.get( Freezing.class );
        Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );

        boolean observe = false;

        for (int i = area.left-1; i <= area.right; i++) {
            for (int j = area.top-1; j <= area.bottom; j++) {
                cell = i + j*Dungeon.level.width();
                if (cur[cell] > 0) {

                    if (freeze != null && freeze.volume > 0 && freeze.cur[cell] > 0){
                        freeze.clear(cell);
                        continue;
                    }

                    if (fire != null && fire.volume > 0 && fire.cur[cell] > 0){
                        fire.clear(cell);
                        continue;
                    }

                    burn( cell );

                    off[cell] = cur[cell] - 1;
                    if (off[cell] <= 0 && flamable[cell]) {

                        Dungeon.level.destroy( cell );

                        observe = true;
                        GameScene.updateMap( cell );

                    }

                    volume += off[cell];

                } else {
                    off[cell] = 0;
                }
            }
        }

        if (observe) {
            Dungeon.observe();
        }
    }

    public static void burn( int pos ) {
        Char ch = Actor.findChar( pos );
        if (ch != null && !ch.isImmune(Fire.class) && !ch.isImmune(Freezing.class)) {
            Buff.affect( ch, Frostburn.class ).reignite( ch );
        }

        Heap heap = Dungeon.level.heaps.get( pos );
        if (heap != null) {
            heap.burn();
            heap.freeze();
        }

        MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
        if (eternalFire != null && eternalFire.volume > 0) {
            eternalFire.clear( pos );
        }

        Plant plant = Dungeon.level.plants.get( pos );
        if (plant != null){
            plant.wither();
        }
    }

    @Override
    public void use( BlobEmitter emitter ) {
        super.use( emitter );
        emitter.pour( FlameParticle.Frostburn.FACTORY, 0.03f );
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}
