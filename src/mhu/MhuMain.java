package mhu;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mhu.ui.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import java.util.*;

import static mindustry.Vars.state;

public class MhuMain extends Mod {
    static public HashMap<String, Integer> cache = new HashMap<>();
    static public int defaultTeam = Team.derelict.id;
    static public float timer = 0, slowTimer = 0, observerSpawns;
    static boolean[] data = {false, false, false, false, false};

    public MhuMain() {
        Events.on(EventType.ClientLoadEvent.class, you ->MhuSettings.buildCategory());

        Events.on(EventType.PlayerJoin.class, ply  -> {
            if(data[1]) {
                if(cache.containsKey(ply.player.uuid()) && data[3]) {
                    ply.player.team(Team.get(cache.get(ply.player.uuid())));
                    ply.player.clearUnit();
                }
                else if(data[2]) ply.player.team(Team.get(defaultTeam));
            }

            updateCache();
        });


        Events.on(EventType.PlayerLeave.class, ply -> updateCache());

        Events.run(EventType.Trigger.update, this::updateLoop);

        Log.info("Match Hosting Utils loaded!");
    }

    public void updateCache(){
        //Only update the cache when someone player list changes
        for(Player p : Groups.player) cache.put(p.uuid(), p.team().id);
    }

    public void updateLoop(){

        if(Time.globalTime >= slowTimer){
            slowTimer = Time.time + (Time.toSeconds * 10f);
            updateSettings();
        }

        if(Time.globalTime <= timer)return;
        timer = Time.time + (Time.toSeconds * 1.5f);

        updateObservers();
    }

    public static void updateSettings(){
        defaultTeam = Core.settings.getInt("mhu-defaultTeam");
        observerSpawns = Core.settings.getInt("mhu-observerSpawns");

        data[0] = Core.settings.getBool("mhu-allowObservers");
        data[1] = Core.settings.getBool("mhu-alterPlayers");
        data[2] = Core.settings.getBool("mhu-assignObserver");
        data[3] = Core.settings.getBool("mhu-reassignTeams");
        data[4] = Core.settings.getBool("mhu-ignoreSelf");
    }

    public void updateObservers(){
        if(!data[0])return;

        Seq<Player> players  = Groups.player.copy();
        players.removeAll(p -> !p.unit().isNull());
        players.removeAll(p -> p.team().cores().size >= 1);
        if(data[4])players.remove(Vars.player);

        if(players.size == 0) return;

        Seq<CoreBuild> cor = new Seq<>();
        for (TeamData t : state.teams.present) cor.addAll(t.cores);
        cor.sort(c-> c.team.id);

        for(Player p : players){
            Unit u = UnitTypes.evoke.create(p.team());

            if(observerSpawns == 1){
                CoreBuild ran = cor.random();
                Tmp.v1.set(ran.x, ran.y);
            } else if(observerSpawns == 2) Tmp.v1.set((state.map.width / 2f) * 8, (state.map.height / 2f) * 8);
            else Tmp.v1.set(0, 0);


            u.set(Tmp.v1);
            u.controller(p);
            u.spawnedByCore(true);
            u.add();
            u.apply(StatusEffects.disarmed, Float.MAX_VALUE);
            u.apply(StatusEffects.fast, Float.MAX_VALUE);
            u.apply(StatusEffects.overdrive, Float.MAX_VALUE);
            u.apply(StatusEffects.overclock, Float.MAX_VALUE);
        }
    }
}
