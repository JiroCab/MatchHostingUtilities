package mhu;

import arc.*;
import arc.util.*;
import mhu.ui.*;
import mhu.util.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.type.*;

import static mhu.MhuVars.*;
import static mindustry.Vars.state;

public class MhuMain extends Mod {

    public MhuMain() {

        Events.on(WorldLoadBeginEvent.class, e -> clear());

        Events.on(GameOverEvent.class , e ->clearShow());

        Events.on(BlockBuildEndEvent.class, e -> {
            if(e.breaking){
                trackedData[e.tile.team().id][4]++;
            }else{
                trackedData[e.tile.team().id][2]++;
            }
            for(ItemStack stack : e.tile.block().requirements){
                if(trackedSpent[e.tile.team().id][stack.item.id] == null){
                    trackedSpent[e.tile.team().id][stack.item.id] = new ItemStack();
                    trackedSpent[e.tile.team().id][stack.item.id].set(stack.item, stack.amount);
                }
                else trackedSpent[e.tile.team().id][stack.item.id].amount += stack.amount;
            }

        });

        Events.on(BlockDestroyEvent.class, e -> trackedData[e.tile.team().id][3]++);

        Events.on(UnitDestroyEvent.class, e -> trackedData[e.unit.team().id][1]++);

        Events.on(UnitCreateEvent.class, e -> trackedData[e.unit.team().id][0]++);

        Events.on(EventType.ClientLoadEvent.class, you ->{
            clear();
            updateSettingsGlobal();
            MhuSettings.buildCategory();
            TimerHandler.loadReminders();
        });

        Events.on(EventType.PlayerJoin.class, ply  -> {
            if(data[1]) {
                if(cache.containsKey(ply.player.uuid()) && data[3]) {
                    ply.player.team(Team.get(cache.get(ply.player.uuid())));
                    ply.player.clearUnit();
                }
                else if(data[2]) ply.player.team(Team.get(defaultTeam));
            }

            observerHandler.updateCache();
        });

        Events.on(EventType.WorldLoadEvent.class, you -> observerHandler.checkValidTeam());
        Events.on(EventType.PlayerLeave.class, ply -> observerHandler.updateCache());

        Events.on(EventType.HostEvent.class, ply -> {
            state.set(State.paused);
        });

        Events.run(EventType.Trigger.update, MhuVars::updateLoop);

        TimerHandler.handleCommands();

        Log.info("Match Hosting Utils loaded!");
    }


}
