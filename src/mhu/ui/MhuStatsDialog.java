package mhu.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mhu.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mhu.MhuVars.*;
import static mindustry.Vars.ui;


public class MhuStatsDialog extends BaseDialog{
    String sortTxt = "", windowTxt;
    int sort = 0, window = 0;
    boolean reverse = false;

    public  MhuStatsDialog(){
        super("@gameover");
        setFillParent(true);

        makeButtonOverlay();

        titleTable.remove();
        titleTable.clear();

        onResize(this::rebuild);
        shown(() -> {
            window = 0;
            rebuild();
            for(Team team : Team.all){
                trackedData[team.id][5] = team.data().unitCount;
            }
        });

    }
    
    void rebuild(){
        cont.clear();
        buildButtons();
        updateSettings();
        
        cont.pane(t -> {
            if(!gatherStats){
                t.table(z -> z.labelWrap(Core.bundle.get("mod.disabled")));
            }else {
                if(window == -1)buildSettings(t);
                else if(window == 1)buildItems(t);
                else if(window == 2)buildUnits(t);
                else buildTotals(t);
            }
        }).scrollX(false).width(Core.graphics.getWidth() * 0.8f).center();
    }

    public void buildTotals(Table t){
        float widMid = Core.graphics.getWidth() * 0.8f,  tabWidth = widMid / 6f, fntScl = 3f;

        cont.table(head -> {
            head.table(z -> {
                z.add(title("@stats.unitsCreated")).width(tabWidth).get().clicked(() -> setSort(4));

                //all this so i dont have a bundle lol
                String diff = Core.bundle.get("editor.sector").replace(":", "");
                String s = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", Core.bundle.get("rules.title.unit")).replace("!", "");

                z.add(title(s )).width(tabWidth).get().clicked(() -> setSort(5));
                z.add(title("@stats.built")).width(tabWidth).get().clicked(() -> setSort(6));
                z.add(title("@stats.destroyed")).width(tabWidth).get().clicked(() -> setSort(7));
                z.add(title("@stats.deconstructed")).width(tabWidth).get().clicked(() -> setSort(8));
                z.add(title( Core.bundle.get("rules.title.unit") +" "+ Core.bundle.get("mode.survival.name"))).width(tabWidth).get().clicked(() -> setSort(9));
            }).growX().center().row();
            head.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        }).width(widMid).row();

        t.setBackground(Styles.black3);
        Seq<Table> tabs = new Seq<>();
        Seq<Team> tracked = new Seq<>();

        for(int i = 0; i < Team.all.length; i++){
            if(trackedData[i][0] + trackedData[i][1] + trackedData[i][2] + trackedData[i][3] + trackedData[i][4] >= 1)tracked.add(Team.get(i));
        }

        if(sort == 1) tracked.sort(s -> s.color.hue());
        else if(sort == 2)  tracked.sort(s ->s.color.saturation());
        else if(sort == 3)  tracked.sort(s ->s.color.sum());
        else if(sort == 4)  tracked.sort(s ->trackedData[s.id][0]);
        else if(sort == 5)  tracked.sort(s ->trackedData[s.id][1]);
        else if(sort == 6)  tracked.sort(s ->trackedData[s.id][2]);
        else if(sort == 7)  tracked.sort(s ->trackedData[s.id][3]);
        else if(sort == 8)  tracked.sort(s ->trackedData[s.id][4]);
        else if(sort == 9)  tracked.sort(s ->trackedData[s.id][5]);
        else tracked.sort(s -> s.id);
        if(reverse) tracked.reverse();

        for(Team team : tracked){
            int i = team.id;
            Table z = colouredTable(Team.get(i).color);
            z.add(title(trackedData[i][0] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            z.add(title(trackedData[i][1] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            z.add(title(trackedData[i][2] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            z.add(title(trackedData[i][3] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            z.add(title(trackedData[i][4] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            z.add(title(trackedData[i][5] + "", fntScl)).grow().height(statsHeight).fontScale(statsFntScl);
            tabs.add(z);
        }

        if(tabs.size == 0){
            Table owo = colouredTable(Color.darkGray);
            owo.labelWrap(Core.bundle.get("empty"));
            t.add(owo).grow().pad(1f).marginRight(50f).row();
        } else for(Table tab : tabs) t.add(tab).grow().pad(1f).marginRight(50f).row();
    }

    public void buildSettings(Table table){
        Runnable[] rebuild = {null};
        boolean[] shouldRebuild = {true};
        table.table(t-> rebuild[0] = () -> {
            t.clear();
            t.left();
            field(t, "mhu-stats.statsFntScl", "@stat.displaysize", shouldRebuild, rebuild, 1.5f);
            field(t, "mhu-stats.statsHeight", "@height", shouldRebuild, rebuild, 60f);
            t.check("mhu-stats-showhidden", b -> {
                settings.put("mhu-stats-showhidden", b);
                statsShowHidden = b;
                rebuild[0].run();
            }).row();

            t.table(tz-> {
                tz.button("@clear", () ->ui.showConfirm("@settings.clear.confirm", MhuVars::clear)).grow();
                tz.button("@editor.generate",
                () ->ui.showConfirm(bundle.get("settings.clear.confirm") + " \n[scarlet]& " + bundle.get("editor.generate")+ " " +  bundle.get("waves.random")+ " " + bundle.get("settings.data"),
                () -> {
                    MhuVars.clear();
                    MhuVars.generateBogusData();
                })).grow();
            }).growX().pad(5f).row();

        }).pad(5f).row();
        rebuild[0].run();
    }

    public void field(Table t, String setting , String display, boolean[] shouldRebuild, Runnable[] rebuild, float def){
        t.add(display).left().padRight(5);
        t.field(Core.settings.getFloat(setting) + "", s ->{
            statsFntScl = Strings.parseFloat(s);
            settings.put(setting, statsFntScl);
            if(shouldRebuild[0]){
                //prevents rebuilding to fast making it deselect the field which is annoying
                shouldRebuild[0] = false;
                Timer.schedule(() ->{
                    rebuild[0].run();
                    shouldRebuild[0] = true;
                },2f);
            }
        }).valid(f -> Strings.parseFloat(f) >= 0).width(150f).left();
        t.button( Icon.trash, (() -> {
            settings.put(setting, def);
            rebuild[0].run();
        }));
        t.row();
    }

    public void buildItems(Table t){
        float widMid = Core.graphics.getWidth() * 0.8f,  tabWidth = widMid / 4f, fntScl = 1.50f;
        //show most top 5 used most 1st

        cont.table(head -> {
            head.table(z -> {
                z.center();
                z.add(title(Core.bundle.get("stat.buildcost") + " " + Core.bundle.get("wavemode.totals"))).width(tabWidth);

                String diff = Core.bundle.get("editor.sector").replace(":", "");
                String owo = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", "").replace("!", "");

                z.add(title(Core.bundle.get("stat.buildcost") + owo)).width(tabWidth);
                z.add(title(Core.bundle.get("rules.title.unit") + owo)).width(tabWidth);
                z.add(title(Core.bundle.get("rules.title.unit") + " " + Core.bundle.get("wavemode.totals"))).width(tabWidth);

            }).center().row();
            head.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        }).row();

        //Total build costs
        Seq<Table> tabs = new Seq<>();
        Seq<Team> tracked = new Seq<>();

        for(int i = 0; i < Team.all.length; i++){
            if(tracked.contains(Team.get(i)))break;

            for(int s = 0; s < spentSize; s++){
                if(tracked.contains(Team.get(i)))break;
                for(int j = 0; j < Vars.content.items().size; j++){
                    if(mhu.MhuVars.trackedSpent[i][j][s] == null) continue;
                    if(trackedSpent[i][j][s].amount > 1){
                        tracked.addUnique(Team.get(i));
                        break;
                    }
                }
            }

        }

        tracked.forEach(team -> {
            int i = team.id;
            Table z = colouredTable(team.color);
            for(int j = 0; j < spentSize; j++){
                Table itms = new Table();
                int row = 0;
                for(int ic = 0; ic < trackedSpent[i].length; ic++){
                    if(trackedSpent[i][ic][j] == null) continue;
                    itms.add(new ItemDisplay(trackedSpent[i][ic][j].item, trackedSpent[i][ic][j].amount, false)).tooltip(trackedSpent[i][ic][j].item.localizedName + "\n" + trackedSpent[i][ic][j].amount);
                    if(row >= 5){
                        row = 0;
                        itms.row();
                    } else  row++;
                }
                z.add(itms).grow().width(tabWidth);
            }
            tabs.add(z);
        });

        if(tabs.size == 0){
            Table owo = colouredTable(Color.darkGray);
            owo.labelWrap(Core.bundle.get("empty"));
            t.add(owo).grow().pad(1f).marginRight(50f).row();
        } else for(Table tab : tabs) t.add(tab).grow().pad(1f).marginRight(50f).row();


        //
        //Total unit costs
    }

    public void buildUnits(Table t){
        float widMid = Core.graphics.getWidth() * 0.8f,  tabWidth = widMid / 2f, fntScl = 1.50f;
        //show most top 5 used most 1st

        cont.table(head -> {
            head.table(z -> {
                String diff = Core.bundle.get("editor.sector").replace(":", "");
                String owo = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", "").replace("!", "");
                z.add(title(Core.bundle.get("rules.title.unit") + " " + Core.bundle.get("wavemode.totals"))).width(tabWidth);
                z.add(title(Core.bundle.get("rules.title.unit") + owo)).width(tabWidth);

            }).center().row();
            head.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        }).row();


        Seq<Team> tracked = new Seq<>();

        for(int i = 0; i < Team.all.length; i++){
            if(tracked.contains(Team.get(i)))continue;

            for(int j = 0; j < Vars.content.items().size; j++){
                if(unitTotal[i][j] + unitTotal[i][j] >= 1 ) tracked.addUnique(Team.get(i));
            }
        }
        Seq<Table> tabs = new Seq<>();


        for(int i = 0; i < tracked.size; i++){
            Table z = colouredTable(Team.get(i).color);
            for(int j = 0; j < 2; j++){
                int[][] read = j == 1 ? unitTotal : unitLost;
                Table itms = new Table();
                int row = 0;
                for(int ic = 0; ic < read[i].length; ic++){
                    if(read[Team.get(i).id][ic] <= 0) continue;
                    UnitType ty = Vars.content.units().get(ic);
                    if(ty.isHidden()) continue;

                    int finalI = i;
                    int finalIc = ic;
                    itms.table(aaaah -> {
                        aaaah.image(ty.uiIcon).scaling(Scaling.bounded).size(45f).center().tooltip(ty.localizedName + "\n" + read[Team.get(finalI).id][finalIc]).pad(5f).row();
                        aaaah.labelWrap(UI.formatAmount( read[Team.get(finalI).id][finalIc])).left().tooltip(ty.localizedName + "\n" + read[Team.get(finalI).id][finalIc]).pad(5f).get().setStyle(Styles.outlineLabel);
                    }).pad(3f);

                    if(row >= 10){
                        row = 0;
                        itms.row();
                    } else  row++;
                }
                z.add(itms).grow().width(tabWidth);
            }
            tabs.add(z);
        }


        if(tabs.size == 0){
            Table owo = colouredTable(Color.darkGray);
            owo.labelWrap(Core.bundle.get("empty"));
            t.add(owo).grow().pad(1f).marginRight(50f).row();
        } else for(Table tab : tabs) t.add(tab).grow().pad(1f).marginRight(50f).row();
    }

    public void buildButtons(){

        buttons.clear();
        buttons.marginBottom(10);
        buttons.defaults().size(180, 64f);

        sortTxt = Core.bundle.get("mhu-teams-sort." + sort);

        windowTxt = Core.bundle.get("wavemode.totals");
        if(window == 1) windowTxt = Core.bundle.get("unit.items");
        if(window == 2) windowTxt = Core.bundle.get("rules.title.unit");
        if(window == -1) windowTxt = "[accent]" + Iconc.settings ;


        if(sort >= 3){
            if(sort == 4) sortTxt = Core.bundle.get("stats.unitsCreated");
            if(sort == 5) {
                //all this so i dont have a bundle lol
                String diff = Core.bundle.get("editor.sector").replace(":", "");
                sortTxt = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", Core.bundle.get("rules.title.unit")).replace("!", "");
            }
            if(sort == 6) sortTxt = Core.bundle.get("stats.built");
            if(sort == 7) sortTxt = Core.bundle.get("stats.destroyed");
            if(sort == 8) sortTxt = Core.bundle.get("stats.deconstructed");
            if(sort == 9) sortTxt = Core.bundle.get("rules.title.unit") +" "+ Core.bundle.get("mode.survival.name");
            if(sort == 10) sortTxt = Core.bundle.get("stat.buildcost") + " " + Core.bundle.get("wavemode.totals");
        }

        buttons.table(z ->{
            z.button(Icon.settings, () -> {
                window = -1;
                rebuild();
            }).size(80, 64f).left().row();
            z.button(windowTxt, () -> {
                window++;
                if(window > 2) window = 0;
                rebuild();
            }).size(180, 64f).row();
            z.button(Core.bundle.get("mhu-teams-sort.base") + sortTxt, () -> {
                sort++;
                if(sort > 3) sort = 0;
                rebuild();
            }).size(180, 64f).row();
            z.button("@back", Icon.left, this::hide).size(180f, 64f);
        }).height(180*1.5f);
        buttons.add(new Element()).width(Core.graphics.getWidth() * 0.88f).right();
        addCloseListener();
    }

    public void setSort (int id){
        if(sort == id )reverse = !reverse;
        else sort = id;
        rebuild();
    }

    public Label title(String title, float scl){
        Label label = new Label(title);
        label.setScale(scl, scl);
        label.setWrap(true);
        label.setAlignment(Align.center);
        label.setStyle(Styles.outlineLabel);
        return label;
    }

    public Label title(String title){
        return title(title, 1f);
    }

    public Table colouredTable (Color colour){
        return new Table() {
            @Override
            public void draw() {
                Draw.color(colour, parentAlpha);
                Fill.rect(x + ((width/2))  , y + (height / 2), width, height);
                Draw.reset();
                super.draw();
            }
        };
    }
    
    public void updateSettings()    {
        statsFntScl = Core.settings.getFloat("mhu-stats.statsFntScl");
        if(statsFntScl < 1){
            Core.settings.put("mhu-stats.statsFntScl", 1.5f);
            statsFntScl = Core.settings.getFloat("mhu-stats.statsFntScl");
        }

        statsHeight = Core.settings.getFloat("mhu-stats.statsHeight");
        if(statsHeight < 1){
            Core.settings.put("mhu-stats.statsHeight", 60f);
            statsHeight = Core.settings.getFloat("mhu-stats.statsHeight");
        }

        menuStats = settings.getBool("mhu-statsMainMenu");
        gatherStats = Core.settings.getBool("mhu-trackPerTeamStats");
    }

    @Override
    public Dialog show() {
        this.window = 0;
        return this.show(Core.scene);
    }
}


