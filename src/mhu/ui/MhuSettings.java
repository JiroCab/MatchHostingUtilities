package mhu.ui;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mhu.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.ui;

public class MhuSettings{


    //Foo's complaint categories
    public static class MhuSettingTable extends SettingsMenuDialog.SettingsTable.Setting{
        public MhuSettingTable(String name){
            super(name);
        }

        public void add(SettingsMenuDialog.SettingsTable table){
            table.table(t -> t.button(Core.bundle.get("setting.mhu-teamObservers.name"), Icon.list, Styles.defaultt, MhuSettings::showTeamPicker).growX()).growX().row();
        }

    }
    public static void buildCategory(){
        ui.settings.addCategory("@settings.mhu.settings", Icon.logic, table ->{
         table.pref(new MhuSettingTable("mhu-settings"));

            table.checkPref("mhu-allowObservers", true);
            table.checkPref("mhu-alterPlayers", true);
            table.checkPref("mhu-assignObserver", true);
            table.checkPref("mhu-reassignTeams", true);
            table.checkPref("mhu-ignoreSelf", true);
            table.sliderPref("mhu-observerSpawns", 1, 0, 2, 1, s -> Core.bundle.get("mhu-observers." + s));
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
                    MhuMain.updateSettings();
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
        if(MhuMain.defaultTeam >= 0){
            button.resizeImage(teamSize);
            button.getImageCell();
            button.getStyle().imageUpColor = Team.get(MhuMain.defaultTeam).color;
            out = Team.get(MhuMain.defaultTeam).emoji.isEmpty() ? MhuMain.defaultTeam + "":  "[white]" +Team.get(MhuMain.defaultTeam).emoji;
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
        if(MhuMain.defaultTeam >= 0) return;
        MhuMain.defaultTeam = Mathf.random(1, Team.all.length);
        Call.sendChatMessage(Core.bundle.format("mhu-team-observers-no-team", MhuMain.defaultTeam, Team.get(MhuMain.defaultTeam).color.toString()));
    }
}
