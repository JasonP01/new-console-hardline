package newconsole.ui.dialogs;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import newconsole.*;
import newconsole.ui.*;

/**
 * A full-blown file browser dialog.
 * @author Mnemotechnician
 */
@SuppressWarnings("CanBeFinal")
public class FileBrowser extends Dialog {

    /**
     * File types that can be read as text
     */
    public static final Seq<String> readableExtensions = Seq.with("txt", "md", "properties", "conf", "config", "log", "prop");
    /**
     * Files containing raw code
     */
    public static final Seq<String> codeExtensions = Seq.with("js", "java", "kt", "json", "hjson", "gradle", "frag", "vert");
    /**
     * Files that can be opened as images
     */
    public static final Seq<String> imageExtensions = Seq.with("png", "jpg", "jpeg", "bmp" /*?*/);

    public InputPrompt inputPrompt;
    public ImageDialog imageDialog;

    public BetterPane mainPane;
    public Table filesTable;

    protected Fi currentDirectory;
    /**
     * The last opened zip file. Used to return from zip file trees
     */
    protected Fi zipEntryPoint;

    /**
     * The file that's being moved/copied. If null, no file is being moved/copied
     */
    protected Fi movedFile;
    /**
     * If this.movedFile is not null, this field indicates whether it's being moved or copied (true or false, respectively)
     */
    protected boolean isMoved = false;


    public FileBrowser() {
        super("");
        closeOnBack();
        setFillParent(true);

        inputPrompt = new InputPrompt();
        imageDialog = new ImageDialog();

        cont.label(() -> {
            if (currentDirectory instanceof ZipFi) {
                return "[darkgrey]" + (zipEntryPoint != null ? zipEntryPoint.name() : "") + ": ZIP FILE ROOT[]" + currentDirectory.absolutePath();
            } else {
                return currentDirectory.absolutePath();
            }
        }).growX().get().setWrap(true);
        cont.row();

        cont.table(bar -> {
            bar.left().defaults().height(50f);

            bar.button(Icon.exit, Styles.defaulti, this::hide).size(50f);

            bar.button("@newconsole.files.save-script", Styles.defaultt, () -> ifNotZip(() -> inputPrompt.prompt("@newconsole.file-name", name -> {
                if (!name.contains(".")) {
                    name = name + ".js"; //no extension - set to .js
                }

                String script = ConsoleVars.console.area.getText();
                var file = currentDirectory.child(name);
                if (!file.exists()) {
                    file.writeString(script, false);
                    rebuild();
                } else {
                    Vars.ui.showConfirm("@newconsole.file-override", () -> {
                        file.writeString(script, false);
                        rebuild();
                    });
                }
            }))).width(250);

            bar.button("@newconsole.files.new-folder", Styles.defaultt, () -> ifNotZip(() -> inputPrompt.prompt("@newconsole.folder-name", name -> {
                var dir = currentDirectory.child(name);
                if (dir.exists()) {
                    Vars.ui.showInfo("@newconsole.already-exists");
                } else {
                    dir.mkdirs();
                    rebuild();
                }
            }))).width(150);

            bar.table(right -> {
                right.right();

                right.button("@newconsole.files-paste", Styles.defaultt, () -> {
                    if (movedFile != null) {
                        var target = currentDirectory.child(movedFile.name());
                        if (target.equals(movedFile)) {
                            Vars.ui.showInfo("@newconsole.same-folder");
                        } else if (target.absolutePath().startsWith(currentDirectory.absolutePath())) {
                            Vars.ui.showInfo("@newconsole.recursive-copy");
                        } else {
                            //aaaaa save my soul qwq
                            Runnable run = () -> {
                                if (isMoved) {
                                    movedFile.moveTo(target);
                                } else {
                                    movedFile.copyTo(target);
                                }
                                movedFile = null;
                                rebuild();
                            };

                            if (target.exists()) {
                                Vars.ui.showConfirm("@newconsole.file-override", run);
                            } else {
                                run.run();
                            }
                        }
                    }
                }).size(90, 50).visible(() -> movedFile != null);
            }).growX();
        }).growX().row();

        //special entry that allows to go to the parent directory
        cont.table(entry -> {
            entry.setBackground(CStyles.filebg);
            entry.center().left().marginBottom(3f).defaults().pad(7f).height(50f);
            entry.touchable = Touchable.enabled;

            entry.image(CStyles.directory).size(50f).marginRight(10f).get().setColor(Color.gray);
            entry.add("@newconsole.files-up");

            entry.clicked(() -> {
                //special case for zip files
                if (currentDirectory.parent() == null && currentDirectory instanceof ZipFi && zipEntryPoint != null) {
                    currentDirectory = zipEntryPoint; //return from the zip file
                    zipEntryPoint = null;
                }

                //root & shared storage (android) directories may be unaccessible. This isn't a failproof way to check but whatsoever.
                if (currentDirectory.parent().list().length > 0) {
                    openDirectory(currentDirectory.parent());
                } else {
                    Log.warn("Cannot access superdirectory " + currentDirectory.parent() + " (no permission?)");
                    Vars.ui.showInfo("@newconsole-no-permission");
                }
            });
        }).growX().row();

        mainPane = new BetterPane(t -> filesTable = t);
        cont.add(mainPane).grow();
    }

    @Override
    public Dialog show(Scene stage, Action action) {
        rebuild();
        return super.show(stage, action);
    }

    /**
     * Returns whether the current directory is inside a zip file
     */
    public boolean isZipTree() {
        return zipEntryPoint != null && currentDirectory instanceof ZipFi;
    }

    /**
     * Runs the runnable if the current directory is not in a zip tree, shows an info popup otherwise
     */
    protected void ifNotZip(Runnable run) {
        if (isZipTree()) {
            Vars.ui.showInfo("@newconsole.zip-not-permitted");
        } else {
            run.run();
        }
    }

    public void rebuild() {
        if (currentDirectory == null || !currentDirectory.exists()) {
            currentDirectory = Vars.dataDirectory;
        }

        filesTable.clear();
        var list = currentDirectory.list();
        //first run: add subdirectories
        for (Fi file : list) {
            if (file.isDirectory()) buildFile(file);
        }
        //second: add files
        for (Fi file : list) {
            if (!file.isDirectory()) buildFile(file);
        }

        //workaround. idk how to fix that
        Spinner.hideAllUnique();
    }

    /**
     * Adds the providen file to the list
     */
    protected void buildFile(Fi file) {
        filesTable.row();
        filesTable.add(new FileEntry(file, it -> {
            if (it.isDirectory()) {
                openDirectory(it);
            } else {
                String ext = it.extension();
                if (ext.equals("zip") || ext.equals("jar")) {
                    if (zipEntryPoint == null) {
                        zipEntryPoint = file; //if it's not null, a zip file has been opened inside of another zip file
                    }
                    openDirectory(it);
                } else {
                    if (readableExtensions.contains(ext) || codeExtensions.contains(ext)) {
                        if (ConsoleVars.console != null) {
                            Vars.ui.showConfirm("@newconsole.open-readable", () -> {
                                ConsoleVars.console.setCode(it.readString());
                                hide();
                            });
                        }
                    } else if (imageExtensions.contains(ext)) {
                        imageDialog.showFor(it);
                    } else {
                        Vars.ui.showConfirm("@newconsole.unknown-format", () -> {
                            ConsoleVars.console.setCode(it.readString());
                            hide();
                        });
                    }
                }
            }
        })).growX();
    }

    /**
     * Sets the current directory to the providen file. If the file is not a directory, it tries to read it as a zip file.
     */
    public void openDirectory(Fi file) {
        if (file == null || !file.exists()) {
            Log.warn("Attempt to open an inexistent directory. Ignored.");
            return;
        }

        if (!file.isDirectory()) {
            try {
                currentDirectory = new ZipFi(file);
            } catch (Exception e) {
                Vars.ui.showException("@newconsole.zip-corrupt", e);
            }
        } else {
            currentDirectory = file;
        }
        rebuild();
    }

    /**
     * A dialog with a single image
     */
    public static class ImageDialog extends Dialog {

        public Label label;
        public Image image;

        public ImageDialog() {
            super("@newconsole.image-preview");
            closeOnBack();

            label = new Label("");
            image = new Image();

            cont.center();
            cont.add(label).row();
            cont.add(image).row();
            cont.button("@newconsole.close", Styles.defaultt, this::hide).fillX();
        }

        /**
         * Loads the providen image and shows it. Shows an error if the file isn't an image
         */
        public void showFor(Fi file) {
            try {
                label.setText(file.name());
                //"new Texture(Fi file)" invokes some sussy native-level methods that, in case of a failure, crash the whole application without a java-level exception
                var pixmap = PixmapIO.readPNG(file);
                var texture = new Texture(pixmap);
                image.setDrawable(new TextureRegion(texture));

                show();
            } catch (Throwable e) {
                Vars.ui.showException("@newconsole.image-corrupt", e);
            }
        }

    }

    /**
     * An element representing a file or a directory
     */
    public class FileEntry extends Table {

        public Fi file;

        public FileEntry(Fi file, Cons<Fi> onclick) {
            this.file = file;

            setBackground(CStyles.filebg);
            center().left().marginBottom(3f).defaults().pad(7f).height(50f);

            //this is a workaround. a widget group would fire an onclick event even if one of it's children fired such an event too. fuck libgdx.
            table(touchable -> {
                touchable.touchable = Touchable.enabled; //such a meme lol

                //icon
                var image = touchable.image(pickIcon(file)).size(50f).marginRight(10f).get();
                image.setColor(file.name().startsWith(".") ? Color.gray : file.isDirectory() ? CStyles.accent : Color.white);

                //name
                var name = touchable.add(file.name()).width(200f).get();
                name.setEllipsis("...");
                if (file.equals(movedFile)) {
                    name.setColor(isMoved ? Color.yellow : Color.green); //yellow = move, green = copy, white = none
                }

                touchable.table(middle -> {
                    middle.right();

                    //file size
                    if (!file.isDirectory()) {
                        middle.add(formatSize(file.length())).get().setColor(Color.gray);
                    }
                }).growX();

                touchable.clicked(() -> onclick.get(file));
            }).growX();

            table(right -> {
                right.right();

                //actions
                right.add(new Spinner("@newconsole.actions", spinner -> {
                    spinner.setBackground(CStyles.filebg);
                    spinner.defaults().height(40f).growX();

                    spinner.button("@newconsole.files-rename", Styles.cleart, () -> ifNotZip(() -> inputPrompt.prompt("@newconsole.file-rename", file.name(), name -> {
                        var target = file.parent().child(name.replaceAll("/", "_"));
                        if (target.exists()) {
                            Vars.ui.showInfo("@newconsole.file-exists");
                        } else {
                            file.moveTo(target);
                            rebuild();
                        }
                    }))).row();

                    spinner.button("@newconsole.files-copy", Styles.cleart, () -> {
                        movedFile = file;
                        isMoved = false;
                        rebuild();
                    }).row();

                    spinner.button("@newconsole.files-move", Styles.cleart, () -> ifNotZip(() -> {
                        movedFile = file;
                        isMoved = true;
                        rebuild();
                    })).row();

                    spinner.button("@newconsole.files-delete", Styles.cleart, () -> ifNotZip(() -> Vars.ui.showConfirm("@newconsole.delete-confirm", () -> {
                        if (file.isDirectory()) {
                            file.deleteDirectory();
                        } else {
                            file.delete();
                        }
                        rebuild();
                    })));
                })).width(200f);
            });
        }

        /**
         * Picks an icon based on the extension of the file, returns a folder icon for directories
         */
        public TextureRegion pickIcon(Fi file) {
            if (file == null || file.isDirectory()) {
                return CStyles.directory;
            }
            String ext = file.extension();
            switch (ext) {
                case "js":
                    return CStyles.fileJs; //this is a js console, after all
                case "zip":
                    return CStyles.fileZip;
                case "jar":
                    return CStyles.fileJar;
                default:
                    break;
            }
            if (readableExtensions.contains(ext)) return CStyles.fileText;
            if (codeExtensions.contains(ext)) return CStyles.fileCode;
            if (imageExtensions.contains(ext)) return CStyles.fileImage;
            return CStyles.fileAny;
        }

        /**
         * Formats file size to a human-readable format, i.e. 334 Mb, 2 Gb
         */
        public String formatSize(long bytes) {
            if (bytes > 1e12)
                return Math.floor(bytes / 1e11) / 10f + " Tb"; //well, i don't think someone will ever get this number
            if (bytes > 1e9) return Math.floor(bytes / 1e8) / 10f + " Gb";
            if (bytes > 1e6) return Math.floor(bytes / 1e5) / 10f + " Mb";
            if (bytes > 1e3) return Math.floor(bytes / 1e2) / 10f + " Kb";
            return bytes + " b";
        }

    }

}
