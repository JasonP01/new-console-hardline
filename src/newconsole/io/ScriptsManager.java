package newconsole.io;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;

/**
 * @author Mnemotechnician
 */
@SuppressWarnings("CanBeFinal")
public class ScriptsManager {

    //public static final String save = "newconsole.save", def = "console/default.save";
    public static final byte startScript = 3, endScript = 4, splitter = 17, eof = 127;

    public static Fi root = Vars.dataDirectory.child("saves");

    public final String name, nameSave;
    public StringMap scripts = new StringMap();

    public ScriptsManager(String name){
        this.name = name;
        this.nameSave = Strings.format("newconsole-scripts-@.save", name);
    }

    public boolean loadSave(Fi save) {
        if (!save.exists() || save.isDirectory()) return false;

        //yeah, I did all the funny code just in case of unexpected modifications
        //also this thing will not break if I'll add something else. and I'll definitely do.
        try (var reads = save.reads()) {
            byte b;
            scripts:
            do {
                b = reads.b();
                if (b == startScript) {
                    String name = reads.str();
                    //find splitter
                    while ((b = reads.b()) != splitter) {
                        if (b == endScript) {
                            Log.warn("illegal EOS, skipping");
                            continue scripts;
                        } else if (b == eof) {
                            Log.warn("Illegal end of file: splitter and script body expected");
                            return false;
                        }
                    }

                    String script = reads.str();
                    scripts.put(name, script);

                    //find end of script, just in case
                    while ((b = reads.b()) != endScript) {
                        if (b == eof) {
                            Log.warn("EOS expected, found EOF. Ignoring.");
                            break scripts;
                        }
                    }
                }
            } while (b != eof);
        } catch (Exception e) {
            Log.warn("Failed to read existing save file. Illegal modification?");
            return false;
        }
        return true;
    }

    /**
     * Save scripts & create a backup
     */
    public void save() {
        //backup
        Fi savef = root.child(nameSave);
        if (savef.exists()) {
            savef.moveTo(root.child(nameSave + ".backup"));
        }
        //save
        var writes = root.child(nameSave).writes();
        scripts.each((name, script) -> {
            writes.b(startScript);
            writes.str(name);
            writes.b(splitter);
            writes.str(script);
            writes.b(endScript);
        });
        writes.b(eof);
        writes.close();
    }

    public void eachScript(Cons2<String, String> cons) {
        scripts.each(cons);
    }

    public void saveScript(String name, String script) {
        scripts.put(name, script);
        save();
    }

    public void deleteScript(String name) {
        scripts.remove(name);
        save();
    }

}
