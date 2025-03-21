package mhu.util;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mhu.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static mhu.MhuVars.*;
import static mindustry.Vars.netServer;

public class TimerHandler{
    //Since its saving a string, using obscure Unicode characters as split indicators just in case
    public static String split =  "\u0309", subSplit = " \u031A";

    public static void handleCommands(){
        netServer.clientCommands.<Player>register("time", "(Mhu) Prints Timer left", (args, player) ->{
          if(!matchActive) player.sendMessage("Match has not started!");
          else player.sendMessage(formatTime() + " left! ");
        });


        netServer.clientCommands.<Player>register("extend", "<seconds>","(Mhu) Extends the timer", (args, player) -> {
            if(player.isLocal() || player.admin){
                try{
                    int t = Integer.parseInt(args[0]);

                    matchClock += t;
                    if(!matchActive)startMatch();

                } catch (Exception x) {
                    player.sendMessage("Invalid seconds!");
                }
            }else player.sendMessage("Only admins/host can use this command!");
        });

        netServer.clientCommands.<Player>register("start", "(Mhu) Starts the Timer", (args, player) -> {
            if(player.isLocal() || player.admin){
                MhuVars.startMatch();
            }else player.sendMessage("Only admins/host can use this command!");
        });
    }

    public static void loopTimer(){
        if(!matchActive)return;

        if(Time.time <= secTimer)return;
        secTimer = Time.time + (Time.toSeconds);
        if(timerData[0]){
            timerData[0] = false;
            return;
        }

        if(matchClock % 10 == 0)updateCache();
        checkReminders();

        if(matchClock <= 0){
            stopMatch();
            return;
        }
        matchClock--;
    }

    public static void updateCache(){
        remindersCache.clear();
        remindersCache = reminders.copy();
        Log.err((matchClock - 10) + " " +  (matchClock + 10));
        for(ReminderHelper remind : remindersCache){
            if(!(remind.time >= matchClock - 10 && remind.time <= matchClock + 10)) remindersCache.remove(remind);
        }
    }

    public static void checkReminders(){
        for(ReminderHelper reminder : remindersCache){
            if(reminder.time == matchClock){
                remindersCache.remove(reminder);
                if(reminder.broadcast) Call.sendChatMessage(reminder.msg);
                else Call.sendMessage(reminder.msg);
            }
        }
    }

    public static void  forceReminderCheck(){
        updateCache();
        checkReminders();
        timerData[0] = true;
    }

    public static String formatTime(){
       return formatTime(matchClock);
    };

    public static String formatTime(int time){
        int t = time;
        StringBuilder out = new StringBuilder();
        if(t <= 0) return "[darkgrey]" + t;


        if(t >= 3600){
            int h = Math.round(t/3600f);
            out.append("[pink]").append(h).append("h[] ");
            t -= 3600 * h;
        }
        if(t >= 60){
            int h = Math.round(t/60f);

            out.append("[red]").append(h).append("m[] ");
            t -= 60 * h;
        }
        if(t < 0) t = Math.abs(60 + t);
        if(t > 0)out.append("[scarlet]").append(t).append("s[]");



        return out.toString();

    };

    public static void  updateSettings(){
        timerData[1] = Core.settings.getBool("mhu-endGamePaused");
        timerData[2] = Core.settings.getBool("mhu-startGamePaused");
        timerData[3] = Core.settings.getBool("mhu-matchStartUnpause");
    }

    public static void reminderDialog(){
        BaseDialog dialog = new BaseDialog("@mhu-set-reminder");
        boolean[] shouldRebuild = {true};
        Runnable[] rebuild = {null};
        dialog.cont.pane(p -> rebuild[0] = () -> {
            p.clear();
            for(int i = 0; i < reminders.size; i++){
                ReminderHelper remind = reminders.get(i);
                p.table(Tex.button, t -> {

                    t.table( j -> {
                        numberi("time:", z -> {
                            remind.time = z;
                            if(shouldRebuild[0]){
                                //prevents rebuilding to fast making it deselect the field which is annoying
                                shouldRebuild[0] = false;
                                Timer.schedule(() -> {
                                    rebuild[0].run();
                                    shouldRebuild[0] = true;
                                }, 1.5f);
                            }
                        }, () -> remind.time, j);
                        Image img =new Image(Icon.cancel);
                        img.clicked(() ->{
                            reminders.remove(remind);
                            rebuild[0].run();
                        });
                        j.add(img).touchable(Touchable.enabled).scaling(Scaling.bounded).padLeft(6f).padRight(6f);
                        check("mhu-broadcast", z -> remind.broadcast = z, () -> remind.broadcast, j);
                    });

                    t.row();
                    t.table( j -> {
                        j.labelWrap("msg: ").left().width(50f);
                        j.field(remind.msg, z -> remind.msg = z)
                            .valid(k -> !(k.contains(split) || k.contains(subSplit)))
                            .color(Color.lightGray).growX().right();
                    }).growX();
                }).pad(8f).row();

            }
        }).scrollX(false);
        rebuild[0].run();

        dialog.addCloseListener();
        dialog.addCloseButton();
        dialog.buttons.button("@add", Icon.add, () ->{
            reminders.add(new ReminderHelper(0 ," owo", true));
            rebuild[0].run();
        }).size(210, 64f);
        dialog.buttons.button(Core.bundle.get("editor.default"), Icon.trash, () ->{
            defaultReminders();
            rebuild[0].run();
        }).size(210, 64f);

        dialog.setHideAction(() -> {
            StringBuilder out = new StringBuilder();
            boolean first = true;
            for(int i = 0; i < reminders.size; i++){

                //Since its saving a string, using obscure Unicode characters as split indicators just in case
                if(first) first = false;
                else out.append(split);
                out.append(reminders.get(i).time).append(subSplit);
                out.append(reminders.get(i).broadcast).append(subSplit);
                out.append(reminders.get(i).msg).append(subSplit);

            }
            Core.settings.put("mhu-Reminder", out.toString());

            return Actions.fadeOut(0.2F, Interp.fade);
        });


        dialog.show();
    }

    public static class ReminderHelper{
        public int time = 1;
        public String msg = "owo";
        public boolean broadcast = true;

        public ReminderHelper(int time, String out, boolean broadcast){
            this.time = time;
            this.msg = out;
            this.broadcast = broadcast;
        }

    }

    public static void loadReminders(){
        String read = Core.settings.getString("mhu-Reminder");
        if(read == null || read.isEmpty()) return;
        String[] out = read.split(split);
        if(out.length ==0 ) return;

        reminders.clear();
        for(String s : out){
            String[] in = s.split(subSplit);
            ReminderHelper data = new ReminderHelper(Integer.parseInt(in[0]), in[2], in[1].equals("true"));
            reminders.add(data);
        }
    }

    public static void defaultReminders(){
        reminders.clear();
        reminders.add(new ReminderHelper(0 ,"[accent]End of match![]", true));
        reminders.add(new ReminderHelper(1 ,"1!", true));
        reminders.add(new ReminderHelper(2 ,"2!", true));
        reminders.add(new ReminderHelper(3 ,"3!", true));
        reminders.add(new ReminderHelper(10 ,"10 seconds!", true));
        reminders.add(new ReminderHelper(30 ,"30 seconds!", true));
        reminders.add(new ReminderHelper(60 ,"1 minute Left!", true));
        reminders.add(new ReminderHelper(600 ,"10 minute Left!", true));
        reminders.add(new ReminderHelper(1800 ,"30 minute Left!", true));
    }

    static void numberi(String text, Intc cons, Intp prov, Table main){
        main.table(t -> {
            t.left();
            t.add(text).left().padRight(5);
            t.field((prov.get()) + "", s -> cons.get(Strings.parseInt(s)))
            .valid(f -> Strings.parseInt(f) >= 0).width(130f).left();
        }).padTop(0).tooltip(formatTime(prov.get()));
    }

    static void check(String text, Boolc cons, Boolp prov, Table main){
        main.check(Core.bundle.get(text), cons).checked(prov.get()).padLeft(100f).tooltip(Core.bundle.getOrNull(text + ".description")).get().right();
    }

}
