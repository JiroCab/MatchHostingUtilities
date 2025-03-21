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
    static public int defaultTeam = -1; //derelict was not ideal since maps with derelict cores for capture lets observers build
    static public float timer = 0, slowTimer = 0, observerSpawns;
    static boolean[] data = {false, false, false, false, false, false};

    public MhuMain() {
        Events.on(EventType.ClientLoadEvent.class, you ->{
            updateSettings();
            MhuSettings.buildCategory();
        });

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

        Events.on(EventType.WorldLoadEvent.class, you -> checkValidTeam());
        Events.on(EventType.PlayerLeave.class, ply -> updateCache());

        Events.run(EventType.Trigger.update, this::updateLoop);

        Log.info("Match Hosting Utils loaded!");
    }

    public void checkValidTeam(){
        updateSettings();
        if(defaultTeam >= 0) return;
        MhuSettings.showTeamPicker();
    }

    public void updateCache(){
        //Only update the cache when someone player list changes
        for(Player p : Groups.player) cache.put(p.uuid(), p.team().id);
    }

    public void updateLoop(){
        if(!Vars.net.server()) return;
        if(Time.globalTime >= slowTimer){
            slowTimer = Time.time + (Time.toSeconds * 10f);
            updateSettings();
        }

        if(Time.globalTime <= timer)return;
        timer = Time.time + (Time.toSeconds * 3f);

        updateObservers();
    }

    public static void updateSettings(){
        defaultTeam = Core.settings.getInt("mhu-defaultTeam", -1);
        observerSpawns = Core.settings.getInt("mhu-observerSpawns");

        data[0] = Core.settings.getBool("mhu-allowObservers");
        data[1] = Core.settings.getBool("mhu-alterPlayers");
        data[2] = Core.settings.getBool("mhu-assignObserver");
        data[3] = Core.settings.getBool("mhu-reassignTeams");
        data[4] = Core.settings.getBool("mhu-ignoreSelf");
        data[5] = Core.settings.getBool("mhu-mono");
    }

    public void updateObservers(){
        if(!data[0])return;

        Seq<Player> players  = Groups.player.copy();
        //you can prob collapse this to one removeAll but rushie is not bright enough and is lazy to do it :p
        players.removeAll(p -> p.team().cores().size >= 1);
        players.removeAll(p -> !p.isAdded()); //Don't try to update players who aren't even in game yet/loaded
        players.removeAll(p -> !p.unit().isNull());
        if(data[4])players.remove(Vars.player);

        if(players.size == 0) return;

        Seq<CoreBuild> cor = new Seq<>();
        for (TeamData t : state.teams.present) cor.addAll(t.cores);
        cor.sort(c-> c.team.id);

        for(Player p : players){
            Unit u =  UnitTypes.evoke.create(p.team());

            Tmp.v1.set(0, 0);
            if(observerSpawns == 1 && !cor.isEmpty()){
                CoreBuild ran = cor.random();
                Tmp.v1.set(ran.x, ran.y);
            } else if(observerSpawns == 2) Tmp.v1.set((state.map.width / 2f) * 8, (state.map.height / 2f) * 8);


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
