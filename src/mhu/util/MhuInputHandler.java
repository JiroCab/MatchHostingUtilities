package mhu.util;

import arc.*;
import arc.KeyBinds.*;
import arc.input.*;
import arc.input.InputDevice.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import mhu.*;
import mhu.ui.*;
import mindustry.gen.*;

import static arc.Core.*;
import static mhu.MhuVars.mhuKeyBinds;
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
        return mhuKeyBinds.get(key) != null && input.keyTap(mhuKeyBinds.get(key).key);
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
            MhuSettings.rebuildKeys[0].run();
        });

        rebindDialog.titleTable.row();

        rebindDialog.titleTable.button("@mhu-unbind", Icon.cancel, () ->{
            rebindDialog.hide();
            Time.runTask(1f, () -> rebind(name, KeyCode.unset));
            MhuSettings.rebuildKeys[0].run();
        } ).size(210f, 64f).pad(5f).get().hovered(() -> {
            no[0] = true;
        });


        rebindDialog.show();
        Time.runTask(1f, () -> rebindDialog.getScene().setScrollFocus(rebindDialog));
    }

    void rebind(KeyBind bind, KeyCode newKey){
        if(rebindKey == null) return;
        rebindDialog.hide();

        Seq<Section> s = Seq.with(mhuKeyBinds.getSections());
        Section section = s.find(z -> z.device.type() == DeviceType.keyboard);

        section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, new Axis(newKey));

        rebindKey = null;
        MhuSettings.rebuildKeys[0].run();
        save();

    }
    public void load(){
        if(MhuVars.definitions == null) return;

        for(KeyBinds.Section sec : mhuKeyBinds.getSections()){
            for(InputDevice.DeviceType type : InputDevice.DeviceType.values()){
                for(KeyBinds.KeyBind def : mhuKeyBinds.getKeybinds()){
                    String rname = "keybind-" + sec.name + "-" + type.name() + "-" + def.name();

                    KeyBinds.Axis loaded = load(rname);

                    if(loaded != null) sec.binds.get(type, OrderedMap::new).put(def, loaded);
                }
            }

            sec.device = input.getDevices().get(Mathf.clamp(settings.getInt(sec.name + "-last-device-type", 0), 0, input.getDevices().size - 1));
        }
    }

    private KeyBinds.Axis load(String name){
        if(settings.getBool(name + "-single", true)){
            KeyCode key = KeyCode.byOrdinal(settings.getInt(name + "-key", KeyCode.unset.ordinal()));
            return key == KeyCode.unset ? null : new KeyBinds.Axis(key);
        }else{
            KeyCode min = KeyCode.byOrdinal(settings.getInt(name + "-min", KeyCode.unset.ordinal()));
            KeyCode max = KeyCode.byOrdinal(settings.getInt(name + "-max", KeyCode.unset.ordinal()));
            return min == KeyCode.unset || max == KeyCode.unset ? null : new KeyBinds.Axis(min, max);
        }
    }

    void save(){
        if(MhuVars.definitions == null) return;

        for(KeyBinds.Section sec : mhuKeyBinds.getSections()){
            for(InputDevice.DeviceType type : sec.binds.keys()){
                for(ObjectMap.Entry<KeyBinds.KeyBind, KeyBinds.Axis> entry : sec.binds.get(type).entries()){
                    String rname = "keybind-" + sec.name + "-" + type.name() + "-" + entry.key.name();
                    save(entry.value, rname);
                }
            }
            settings.put(sec.name + "-last-device-type", input.getDevices().indexOf(sec.device, true));
        }
    }

    private void save(Axis axis, String name){
        settings.put(name + "-single", axis.key != null);

        if(axis.key != null){
            settings.put(name + "-key", axis.key.ordinal());
        }else{
            settings.put(name + "-min", axis.min.ordinal());
            settings.put(name + "-max", axis.max.ordinal());
        }
    }

}
