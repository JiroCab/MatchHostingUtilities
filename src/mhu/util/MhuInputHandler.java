package mhu.util;

import arc.*;
import arc.KeyBinds.*;
import arc.input.*;
import arc.input.InputDevice.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import mhu.*;
import mindustry.gen.*;

import static arc.Core.*;
import static mindustry.Vars.ui;

public class MhuInputHandler{
    protected Dialog rebindDialog;
    protected KeyBind rebindKey = null;

    public void update(){
        if ((ui.chatfrag.shown() || scene.getKeyboardFocus() != null) || ui.consolefrag.shown())return;

        if(MhuiKeyTap(MhuBinding.show_stats)){
            MhuVars.stats.show();
        }
    }

    public boolean MhuiKeyTap(KeyBind key){
        return MhuVars.mhuKeyBinds.get(key) != null && input.keyTap(MhuVars.mhuKeyBinds.get(key).key);
    }

    public void openDialog(KeyBinds.KeyBind name){
        rebindDialog = new Dialog(bundle.get("keybind.press"));

        rebindKey = name;

        rebindDialog.titleTable.getCells().first().pad(4);
        final boolean[] no = {false};

        rebindDialog.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(Core.app.isAndroid() || no[0]) return false;
                rebind(name, button);
                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode){
                rebindDialog.hide();
                if(keycode == KeyCode.escape) return false;
                rebind( name, keycode);
                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                rebindDialog.hide();
                rebind(name, KeyCode.scroll);
                return false;
            }
        });

        rebindDialog.titleTable.defaults().size(210f, 64).margin(5).pad(5f).row();

        rebindDialog.titleTable.button("@back", Icon.left, () -> rebindDialog.hide()).size(210f, 64f).pad(5f).get().hovered(() -> {
            no[0] = true;
        });

        rebindDialog.titleTable.row();

        rebindDialog.titleTable.button("@mhu-unbind", Icon.cancel, () ->{
            rebindDialog.hide();
            Time.runTask(1f, () -> rebind(name, KeyCode.unknown));
        } ).size(210f, 64f).pad(5f).get().hovered(() -> {
            no[0] = true;
        });


        rebindDialog.show();
        Time.runTask(1f, () -> rebindDialog.getScene().setScrollFocus(rebindDialog));
    }

    void rebind(KeyBind bind, KeyCode newKey){
        if(rebindKey == null) return;
        rebindDialog.hide();

        Seq<Section> s = Seq.with(keybinds.getSections());
        Section section = s.find(z -> z.device.type() == DeviceType.keyboard);

        section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, new Axis(newKey));

        rebindKey = null;

    }
}
