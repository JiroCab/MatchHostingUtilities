package mhu;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mhu.ui.*;
import mhu.util.*;
import mhu.util.TimerHandler.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.type.*;

import java.util.*;

import static mindustry.Vars.state;

public class MhuVars{
    public static MhuStatsDialog stats = new MhuStatsDialog();
    public static ObserverHandler observerHandler = new ObserverHandler();
    public static MhuInputHandler inputHandler = new MhuInputHandler();

    public static Seq<ReminderHelper> reminders = new Seq<>();
    public static Seq<ReminderHelper> remindersCache = new Seq<>();
    public static HashMap<String, Integer> cache = new HashMap<>();
    public static int
        defaultTeam = -1,  //derelict was not ideal since maps with derelict cores for capture lets observers build
        matchClock = -1;
    public static float timer = 0, slowTimer = 0, secTimer, observerSpawns, statsFntScl, statsHeight;
    public static boolean[] data = {false, false, false, false, false, false}, timerData = {false, false, false, false};
    public static boolean matchActive = false, statsShowing = false, menuStats = false, gatherStats = false, statsShowHidden = false;

    //Unit made, units lost, building built, building lost, building decon
    public static int[][] trackedData;
    public static int[][] unitTotal = new int[Team.all.length][Vars.content.units().size], unitLost = new int[Team.all.length][Vars.content.units().size];
    public static final int spentSize = 4;
    //Buildcost, built lost, unitCost, built lost
    public static  ItemStack[][][] trackedSpent = new ItemStack[Team.all.length][Vars.content.items().size][spentSize];
    //this is madness

    public static arc.KeyBinds mhuKeyBinds = new KeyBinds();
    private KeyBinds.KeyBind[] definitions;

    public static void clearShow(){
        stats.show();
    }
    
    public static void clear(){
        remindersCache.clear();
        trackedData = new int[Team.all.length][6];
        trackedSpent = new ItemStack[Team.all.length][Vars.content.items().size][spentSize];
        unitTotal = new int[Team.all.length][Vars.content.units().size];
        unitLost = new int[Team.all.length][Vars.content.units().size];


        matchClock = Core.settings.getInt("mhu-match-clock");
        if(matchClock <= -1)matchClock = 1800;
        matchActive = false;

    }

    public static void updateLoop(){
        TimerHandler.loopTimer();
        observerHandler.updateLoop();
    }

    public static void startMatch(){
        state.set(State.playing);
        secTimer = Time.time;

        //so this is handled instantly
        TimerHandler.forceReminderCheck();
        matchActive = true;
    }

    public static void stopMatch(){
        matchActive = false;
        if(timerData[1])state.set(State.paused);
    }

    public static void  updateSettingsGlobal(){
        ObserverHandler.updateSettings();
        TimerHandler.updateSettings();
        stats.updateSettings();
    }

    public static void firstLoadSets(){
        TimerHandler.defaultReminders();
    }

    public static void generateBogusData(){
        for(int t = 1; t < 100; t++){
            trackedData[t][0] = Team.all.length - t;
            for(int i = 1; i < 6; i++){
                trackedData[t][i] = Mathf.random(0, 5000);
            }
            for(int i = 0; i < Vars.content.items().size; i++){
                if(Mathf.randomBoolean(0.8f)){
                    trackedSpent[t][i][0] = new ItemStack();
                    trackedSpent[t][i][0].set(Vars.content.item(i), Mathf.random(0, 5000));
                }
                trackedSpent[t][i][3] = new ItemStack();
                trackedSpent[t][i][3].set(Vars.content.item(i), Mathf.random(0, 5000));
            }
            for(int i = 0; i < 15; i++){
                trackedSpent[t][i][1] = new ItemStack();
                trackedSpent[t][i][1].set(Vars.content.item(i), Mathf.random(0, 5000));
                trackedSpent[t][i][2] = new ItemStack();
                trackedSpent[t][i][2].set(Vars.content.item(i), Mathf.random(0, 5000));
            }
            for(int i = 0; i < Vars.content.units().size; i++){
                unitLost[t][i] = Mathf.random(0 ,6000);
                unitTotal[t][i] = Mathf.random(0 ,1009587369);
            }

        }
    }
}
