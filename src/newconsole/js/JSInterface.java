package newconsole.js;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import mindustry.*;
import newconsole.*;
import newconsole.io.*;
import newconsole.ui.dialogs.*;

/**
 * This class allows JS scripts to interact with modded classes.
 * @author Mnemotechnician
 */
@SuppressWarnings("unused")
public class JSInterface {

    public static NewConsoleMod getMod() {
        return (NewConsoleMod) Vars.mods.getMod(NewConsoleMod.class).main;
    }

    public static Console getConsole() {
        return ConsoleVars.console;
    }

    public static SavesDialog getSavesDialog() {
        return ConsoleVars.saves;
    }

    public static void loadScripts(Fi file) {
        ScriptsManager.loadSave(file);
    }

    public static void saveAll() {
        ScriptsManager.save();
    }

    public static StringMap getScriptsMap() {
        return ScriptsManager.scripts;
    }

    public static void eachScript(Cons2<String, String> cons) {
        ScriptsManager.eachScript(cons);
    }

    public static void saveScript(String name, String script) {
        ScriptsManager.saveScript(name, script);
    }

    public static void deleteScript(String name) {
        ScriptsManager.deleteScript(name);
    }

    public static void checkUpdates() {
        getMod().checkUpdates();
    }

}
