package mhu;

import arc.*;
import arc.util.*;
import mhu.ui.*;
import mhu.util.*;
import mindustry.*;
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

        Events.on(BlockBuildEndEvent.class, e -> {
            if(!gatherStats) return;
            if(e.breaking){
                trackedData[e.tile.team().id][4]++;
            }else{
                trackedData[e.tile.team().id][2]++;
            }
            for(ItemStack stack : e.tile.block().requirements){
                if(trackedSpent[e.tile.team().id][stack.item.id][0] == null){
                    trackedSpent[e.tile.team().id][stack.item.id][0] = new ItemStack();
                    trackedSpent[e.tile.team().id][stack.item.id][0].set(stack.item, stack.amount);
                }
                else trackedSpent[e.tile.team().id][stack.item.id][0].amount += stack.amount;
            }


        });

        Events.on(BlockDestroyEvent.class, e ->{
            if(!gatherStats) return;
            trackedData[e.tile.team().id][3]++;
            for(ItemStack stack : e.tile.block().requirements){
                if(trackedSpent[e.tile.team().id][stack.item.id][1] == null){
                    trackedSpent[e.tile.team().id][stack.item.id][1] = new ItemStack();
                    trackedSpent[e.tile.team().id][stack.item.id][1].set(stack.item, stack.amount);
                }
                else trackedSpent[e.tile.team().id][stack.item.id][1].amount += stack.amount;
            }

        });

        Events.on(UnitDestroyEvent.class, e ->{
            if(!gatherStats) return;
            trackedData[e.unit.team().id][1]++;
            for(ItemStack stack : e.unit.type.getTotalRequirements()){
                if(trackedSpent[e.unit.team().id][stack.item.id][2] == null){
                    trackedSpent[e.unit.team().id][stack.item.id][2] = new ItemStack();
                    trackedSpent[e.unit.team().id][stack.item.id][2].set(stack.item, stack.amount);
                }
                else trackedSpent[e.unit.team().id][stack.item.id][2].amount += stack.amount;
            }
            unitLost[e.unit.team.id][e.unit.type.id]++;
        });

        Events.on(UnitCreateEvent.class, e -> {
            if(!gatherStats) return;
            trackedData[e.unit.team().id][0]++;

            for(ItemStack stack : e.unit.type.getTotalRequirements()){
                if(trackedSpent[e.unit.team().id][stack.item.id][3] == null){

                    trackedSpent[e.unit.team().id][stack.item.id][3] = new ItemStack();
                    trackedSpent[e.unit.team().id][stack.item.id][3].set(stack.item, stack.amount);
                }
                else trackedSpent[e.unit.team().id][stack.item.id][3].amount += stack.amount;
            }
            unitTotal[e.unit.team.id][e.unit.type.id]++;
        });


        Events.on(EventType.ClientLoadEvent.class, you ->{
            clear();
            updateSettingsGlobal();
            MhuSettings.buildCategory();
            TimerHandler.loadReminders();
            mhuKeyBinds.setDefaults(MhuBinding.values());
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
            if(timerData[2]) state.set(State.paused);
        });

        Events.run(EventType.Trigger.update, MhuVars::updateLoop);

        Events.on(EventType.StateChangeEvent.class, ply -> {
            if(!menuStats)return;
            if(!statsShowing){
                statsShowing = true;
                Timer.schedule(() ->{
                    statsShowing = false;
                    if(Vars.state.isMenu()) stats.show();
                },1.5f);
            }
        });


        Events.on(EventType.GameOverEvent.class, ply -> {
            statsShowing = true;
            Timer.schedule(() -> stats.show(),3f);
        });

        TimerHandler.handleCommands();

        Log.info("Match Hosting Utils loaded!");
    }


}
