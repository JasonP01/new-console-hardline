package newconsole.ui;

import arc.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;

/**
 * Inherited from my previous mod, NewControls.
 * @author Mnemotechnician
 */
@SuppressWarnings({"CanBeFinal", "unused"})
public class Spinner extends TextButton {

    public static Collapser lastCollapser;

    public Collapser col;
    public BetterPane pane;
    public Table table;
    public Image image;

    /**
     * Whether to remove collapser if any of ancestors are invisible / untouchable
     */
    public boolean autoHide = true;
    /**
     * Whether this collapser should be hidden if another unique collapser is displayed and vice versa
     */
    public boolean unique;
    protected float collapseTime = 0.4f;
    protected float padW = 16f, padH = 16f; //padding cus else it looks ugly

    Timer.Task hideTask;

    public Spinner(String header, TextButton.TextButtonStyle style, boolean unique, Cons<Table> constructor) {
        super(header, style);
        this.unique = unique;

        add(image = new Image(Icon.downOpen)).size(Icon.downOpen.imageSize() * Scl.scl(1f)).padLeft(padW / 2f).left();
        //workaround: anuke likes to change signatures of functions to blow up the mods that use them
        var temp = getCells().get(0);
        getCells().set(0, getCells().get(1));
        getCells().set(1, temp);

        col = new Collapser(base -> {
            pane = new BetterPane(t -> {
                t.left();
                this.table = t;
                if (constructor != null) constructor.get(t);
            });
            base.add(pane).growX().scrollX(false);
        }, true).setDuration(collapseTime);

        clicked(() -> {
            col.toggle();
            if (col.isCollapsed()) {
                hide(true);
            } else {
                show(true);
            }
        });

        update(() -> {
            setChecked(!col.isCollapsed());
            image.setDrawable(!col.isCollapsed() ? Icon.upOpen : Icon.downOpen);
        });

        col.update(() -> {
            if (unique && lastCollapser != col) {
                hide(true);
            }

            if (col.getScene() != null) {
                col.visible = true;
                col.toFront();
                col.color.a = parentAlpha * color.a;

                var reverse = false;
                var pos = localToStageCoordinates(Tmp.v1.set(0, -table.getPrefHeight()));
                if (pos.y <= 0 && pos.y + table.getPrefHeight() * 2 + height < Core.scene.getHeight()) {
                    pos.y += table.getPrefHeight() + height;
                    reverse = true;
                }

                var freeHeight = reverse
                        ? Core.scene.getHeight() - localToStageCoordinates(Tmp.v2.set(0, height)).y
                        : localToStageCoordinates(Tmp.v2.setZero()).y;

                col.setSize(width, Math.min(freeHeight, table.getPrefHeight()));
                pane.setSize(width, Math.min(freeHeight, table.getPrefHeight()));
                table.setSize(width, Math.min(freeHeight, table.getPrefHeight()));
                col.setPosition(pos.x, pos.y);
                col.validate();
            }

            if (autoHide && col.getScene() != null) {
                //find any invisible or not touchable ancestors, hide if found
                Element current = this;
                while (true) {
                    if (current.parent == Core.scene.root) {
                        break;
                    } else if (!current.visible || current.touchable == Touchable.disabled || current.parent == null) {
                        hide(false);
                        break;
                    }
                    current = current.parent;
                }
            }
        });
    }

    public Spinner(String header, TextButton.TextButtonStyle style, Cons<Table> constructor) {
        this(header, style, true, constructor);
    }

    public Spinner(String header, boolean unique, Cons<Table> constructor) {
        this(header, Styles.flatTogglet, unique, constructor);
    }

    public Spinner(String header, Cons<Table> constructor) {
        this(header, Styles.flatTogglet, true, constructor);
    }

    /**
     * Makes every unique spinner collapse
     */
    public static void hideAllUnique() {
        lastCollapser = null;
    }

    public void show(boolean animate) {
        if (hideTask != null) hideTask.cancel();

        col.setCollapsed(false, animate);

        Scene prevStage = col.getScene();
        if (prevStage != null) prevStage.root.removeChild(col);
        Scene stage = getScene();
        if (stage == null) return;
        stage.add(col);

        col.toFront();
        toFront();

        if (unique) {
            lastCollapser = col;
        }
    }

    public void hide(boolean animate) {
        col.setCollapsed(true, animate);
        Scene stage = getScene();

        if (stage != null) {
            if (animate) {
                hideTask = Timer.schedule(() -> {
                    stage.root.removeChild(col);
                    hideTask = null;
                }, collapseTime);
            } else {
                stage.root.removeChild(col);
            }
        }
    }

    @Override
    public float getPrefWidth() {
        return super.getPrefWidth() + padW;
    }

    @Override
    public float getPrefHeight() {
        return super.getPrefWidth() + padH;
    }

}
