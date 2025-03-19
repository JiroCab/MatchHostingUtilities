package mhu.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static mhu.MhuVars.*;


public class MhuStatsDialog extends BaseDialog{
    String sortTxt = "", windowTxt;
    int sort = 0, window = 0;

    public  MhuStatsDialog(){
        super("@gameover");
        setFillParent(true);

        makeButtonOverlay();

        titleTable.remove();
        titleTable.clear();

        onResize(this::rebuild);
        shown(this::rebuild);

    }


    void rebuild(){
        cont.clear();
        buildButtons();



        cont.pane(t -> {
            if(!Core.settings.getBool("mhu-trackPerTeamStats")){
                t.table(z -> z.labelWrap(Core.bundle.get("mod.disabled")));
            }else if (trackedData == null){
                t.table(z -> z.labelWrap(Core.bundle.get("empty")));
            }else {
                if(window == 1)buildItems(t);
                else buildTotals(t);
            }
        }).scrollX(false).width(Core.graphics.getWidth() * 0.8f).center();
    }


    public void buildTotals(Table t){
        float widMid = Core.graphics.getWidth() * 0.8f,  tabWidth = widMid / 6f, fntScl = 3f;

        cont.table(head -> {
            head.table(z -> {
                z.center();
                z.add(title("@stats.unitsCreated")).width(tabWidth).get().clicked(() -> setSort(4));

                //all this so i dont have a bundle lol
                String diff = Core.bundle.get("editor.sector").replace(":", "");
                String s = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", Core.bundle.get("rules.title.unit")).replace("!", "");

                z.add(title(s )).width(tabWidth).get().clicked(() -> setSort(5));
                z.add(title("@stats.built")).width(tabWidth).get().clicked(() -> setSort(6));
                z.add(title("@stats.destroyed")).width(tabWidth).get().clicked(() -> setSort(7));
                z.add(title("@stats.deconstructed")).width(tabWidth).get().clicked(() -> setSort(8));
                z.add(title( Core.bundle.get("rules.title.unit") + Core.bundle.get("mode.survival.name"))).get().clicked(() -> setSort(9));
            }).center().row();
            head.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        }).row();

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
        else tracked.sort(s -> s.id);

        for(Team team : tracked){
            int i = team.id;
            Table z = colouredTable(Team.get(i).color, 0.7f, 0.25f, 6);
            z.add(title(trackedData[i][0] + "", fntScl)).grow().width(tabWidth);
            z.add(title(trackedData[i][1] + "", fntScl)).grow().width(tabWidth);
            z.add(title(trackedData[i][2] + "", fntScl)).grow().width(tabWidth);
            z.add(title(trackedData[i][3] + "", fntScl)).grow().width(tabWidth);
            z.add(title(trackedData[i][4] + "", fntScl)).grow().width(tabWidth);
            z.add(title(trackedData[i][4] + "", fntScl)).grow().width(tabWidth);
            tabs.add(z);
        }

        for(Table tab : tabs){
            t.add(tab).grow().pad(1f).marginRight(50f).row();
        }


    }

    public void buildItems(Table t){
        float widMid = Core.graphics.getWidth() * 0.8f,  tabWidth = widMid / 6f, fntScl = 1.50f;
        //show most top 5 used most 1st

        cont.table(head -> {
            head.table(z -> {
                z.center();
                z.add(title(Core.bundle.get("stat.buildcost") + " " + Core.bundle.get("wavemode.totals"))).width(tabWidth);

            }).center().row();
            head.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        }).row();

        //Total build costs
        Seq<Table> tabs = new Seq<>();
        Seq<Team> tracked = new Seq<>();

        for(int i = 0; i < Team.all.length; i++){
            if(tracked.contains(Team.get(i)))break;

            for(int j = 0; j < Vars.content.items().size; j++){
                if(mhu.MhuVars.trackedSpent[i][j] == null) continue;
                if(trackedSpent[i][j].amount > 1){
                    tracked.addUnique(Team.get(i));
                    break;
                }
            }
        }

        for(int i = 0; i < tracked.size; i++){
            Table z = colouredTable(Team.get(i).color, 0.7f, tabWidth, 6);
            Table itms = new Table();
            int row = 0;
            for(int ic = 0; ic < trackedSpent[i].length; ic++){
                if(trackedSpent[i][ic] == null) continue;
                itms.add(new ItemDisplay(trackedSpent[i][ic].item, trackedSpent[i][ic].amount, false)).tooltip(trackedSpent[i][ic].item.localizedName + "\n" + trackedSpent[i][ic].amount);
                if(row >= 5){
                    row = 0;
                    itms.row();
                } else  row++;
            }
            z.add(itms).grow().width(tabWidth);
            tabs.add(z);
        }

        for(Table tab : tabs){
            t.add(tab).grow().pad(1f).marginRight(50f).row();
        }


        //
        //Total unit costs
    }

    public void buildUnits(Table t){
        //Per type counts
        // Survived
        //
    }

    public void buildButtons(){

        buttons.clear();
        buttons.marginBottom(10);
        buttons.defaults().size(180, 64f);

        sortTxt = Core.bundle.get("mhu-teams-sort." + sort);

        windowTxt = Core.bundle.get("wavemode.totals");
        if(window == 1) windowTxt = Core.bundle.get("unit.items");
        if(window == 2) windowTxt = Core.bundle.get("rules.title.unit");


        if(sort >= 4){
            if(sort == 4) sortTxt = Core.bundle.get("stats.unitsCreated");
            if(sort == 5) {
                //all this so i dont have a bundle lol
                String diff = Core.bundle.get("editor.sector").replace(":", "");
                sortTxt = Core.bundle.get("sector.lost").replace(diff, "").replace("[accent]{0}[white]", Core.bundle.get("rules.title.unit")).replace("!", "");
            }
            if(sort == 6) sortTxt = Core.bundle.get("stats.built");
            if(sort == 7) sortTxt = Core.bundle.get("stats.destroyed");
            if(sort == 8) sortTxt = Core.bundle.get("stats.deconstructed");
            if(sort == 9) sortTxt = Core.bundle.get("stat.buildcost") + " " + Core.bundle.get("wavemode.totals");
        }
        buttons.table(z ->{
            z.button(windowTxt, () -> {
                window++;
                if(window > 3) window = 0;
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
        Sounds.click.play();
        sort = id;
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

    public Table colouredTable (Color colour, float alpha, float distance, int amount){
        return new Table() {
            @Override
            public void draw() {
                Draw.color(colour, alpha * parentAlpha);
                float w = width/ amount;
                for(int i = 0; i < amount; i++){
                    Fill.rect(x + ((w*i/2))  , y + (height / 2), w * 0.9f, height);
                }
                Draw.reset();
                super.draw();
            }
        };
    }
}


