package mhu.ui;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mhu.*;
import mhu.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.ui;

public class MhuSettings{
    public static Runnable[] rebuildKeys = {null};
    //Foo's complaint categories
    public static class MhuSettingTable extends SettingsMenuDialog.SettingsTable.Setting{
        int type = -1;

        public MhuSettingTable(String name){
            super(name);
        }
        public MhuSettingTable(String name, int type){
            super(name);
            this.type = type;
        }

        public void add(SettingsMenuDialog.SettingsTable table){
            switch (type) {
                case -2 -> table.table(t-> {
                    t.add("[accent]" + Core.bundle.get(name)).row();
                    t.image().color(Pal.accent).height(3f).padTop(5).padBottom(5).growX().row();
                }).padTop(5f).growX().row();
                case 3 -> table.table(t -> t.button(Core.bundle.get("stats"), Icon.list, Styles.defaultt, () -> MhuVars.stats.show()).growX()).growX().padLeft(10f).padRight(10f).padTop(2f).padBottom(2f).row();
                case 2 ->
                    table.table(Tex.buttonDisabled, t ->{
                        rebuildKeys[0] = () -> {
                            t.clear();
                            String name = "mhu-statsKey";
                            t.add(bundle.get("keybind." + name + ".name", Strings.capitalize(name)), Color.white).left().padRight(40).padLeft(8);
                            //t.label(() -> cuiKeyBinds.get(section, keybind).key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);
                            t.label(() -> MhuVars.mhuKeyBinds.get(MhuBinding.show_stats).key.name()).color(Pal.accent).left().minWidth(90).padRight(20);

                            t.button("@settings.rebind",  Styles.defaultt, () -> {
                                MhuVars.inputHandler.openDialog(MhuBinding.show_stats);
                            }).width(130f);
                        };
                        rebuildKeys[0].run();
                    }).pad(3f).margin(4f).padTop(5f).growX().row();
                case 1 ->{
                    table.table(t->{
                        t.clear();
                        t.left();
                        t.table( ta ->{
                            ta.add(title).left().padRight(5);
                            ta.field(Core.settings.getInt("mhu-match-clock") + "", s ->{
                                MhuVars.matchClock = Strings.parseInt(s);
                                settings.put("mhu-match-clock", MhuVars.matchClock);
                                MhuVars.updateSettingsGlobal();
                            }).valid(f -> Strings.parseInt(f) >= 0).color(Color.white).minWidth(200).padLeft(5f);
                            ta.label(() ->TimerHandler.formatTime(settings.getInt("mhu-match-clock")));
                        }).growX().left();
                    }).growX().pad(5f).row();

                }
                case 0 -> table.table(t -> t.button(Core.bundle.get("setting.mhu-reminderConfig.name"), Icon.settings, Styles.defaultt, TimerHandler::reminderDialog).growX()).growX().padLeft(10f).padRight(10f).padTop(2f).padBottom(2f).row();
                default -> table.table(t -> t.button(Core.bundle.get("setting.mhu-teamObservers.name"), Icon.list, Styles.defaultt, MhuSettings::showTeamPicker).growX()).growX().padLeft(10f).padRight(10f).padTop(2f).padBottom(2f).row();
            }
        }

    }

    public static void buildCategory(){
        ui.settings.addCategory("@settings.mhu.settings", Icon.logic, table ->{

            table.pref(new MhuSettingTable("mhu-observers", -2));
            table.pref(new MhuSettingTable("mhu-teamObservers"));
            table.checkPref("mhu-alterPlayers", true);
            table.checkPref("mhu-allowObservers", true);
            table.checkPref("mhu-assignObserver", true);
            table.checkPref("mhu-reassignTeams", true);
            table.checkPref("mhu-ignoreSelf", true);
            table.sliderPref("mhu-observerSpawns", 1, 0, 2, 1, s -> Core.bundle.get("mhu-observers." + s));

            table.pref(new MhuSettingTable("mhu-stats", -2));
            table.checkPref("mhu-trackPerTeamStats", true);
            table.checkPref("mhu-statsMainMenu", true);
            table.pref(new MhuSettingTable("mhu-statHotkey", 2));
            table.pref(new MhuSettingTable("mhu-statShow", 3));

            table.pref(new MhuSettingTable("mhu-timer", -2));
            table.pref(new MhuSettingTable("mhu-matchDuration", 1));
            table.pref(new MhuSettingTable("mhu-reminderConfig", 0));
            table.checkPref("mhu-formatColorsAlt", true);
            table.checkPref("mhu-endGamePaused", true);
            table.checkPref("mhu-startGamePaused", true);
            table.checkPref("mhu-matchStartUnpause", true);

            settings.getBoolOnce("mhu-firstLoad", MhuVars::firstLoadSets);
            //reminder dialog
        });
    }

    public static void showTeamPicker(){
        int teamSize = 55;
        int[] icons = {0};

        BaseDialog dialog = new BaseDialog("@mhu-team-picker.name");
        dialog.centerWindow();

        dialog.closeOnBack(MhuSettings::hideHandler);

        dialog.cont.setOrigin(Align.center);
        dialog.cont.table(ta -> ta.pane(t ->{
            int max = (Vars.mobile || Vars.testMobile) ? 4 : 7;
            for (int id = 0; id < Team.all.length ; id++){
                int finalId = id;

                ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
                button.clicked(() ->{
                    Core.settings.put("mhu-defaultTeam", finalId);
                    ObserverHandler.updateSettings();
                    dialog.hide();
                });
                button.resizeImage(teamSize);
                button.getImageCell();
                button.getStyle().imageUpColor = Team.get(id).color;

                Label lab = new Label(Team.get(id).emoji.isEmpty() ? finalId + "":  "[white]" +Team.get(id).emoji);

                lab.touchable(() -> Touchable.disabled);
                lab.setAlignment(Align.center);
                t.stack(
                button,
                lab
                ).size(teamSize).margin(5f).grow().get();

                if (icons[0] >= max){
                    t.row();
                    icons[0] = 0;
                } else icons[0]++;
            }
        }).size(ta.getWidth(), ta.getWidth()).scrollX(false)).growX().growY().center().top().row();

        ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
        String out = "[accent]" + Iconc.cancel;
        if(MhuVars.defaultTeam >= 0){
            button.resizeImage(teamSize);
            button.getImageCell();
            button.getStyle().imageUpColor = Team.get(MhuVars.defaultTeam).color;
            out = Team.get(MhuVars.defaultTeam).emoji.isEmpty() ? MhuVars.defaultTeam + "":  "[white]" +Team.get(MhuVars.defaultTeam).emoji;
        } else button.getStyle().imageUpColor = Color.scarlet;

        Label lab = new Label(out);
        lab.touchable(() -> Touchable.disabled);
        lab.setAlignment(Align.center);


        dialog.cont.table(t ->{
            t.stack(
                button,
                lab
            ).size(teamSize).margin(5f).grow().get();
            t.button("@back", Icon.left, () -> {
                dialog.hide();
                hideHandler();
            }).size(220f, 55f);
        }).padTop(-1f).bottom();

        dialog.closeOnBack();
        dialog.show();
    }

    public static void hideHandler(){
        if(MhuVars.defaultTeam >= 0) return;
        MhuVars.defaultTeam = Mathf.random(1, Team.all.length);
        Call.sendChatMessage(Core.bundle.format("mhu-team-observers-no-team", MhuVars.defaultTeam, Team.get(MhuVars.defaultTeam).color.toString()));
    }


}
