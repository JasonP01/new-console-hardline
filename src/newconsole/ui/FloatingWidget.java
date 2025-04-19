package newconsole.ui;

import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;

/**
 * Table that can be dragged across a WidgetGroup.
 * @author Mnemotechnician
 */
@SuppressWarnings("CanBeFinal")
public class FloatingWidget extends Table {

    public static float draggedAlpha = 0.45f;

    public ImageButton dragger;
    public boolean isDragging = false;

    public FloatingWidget() {
        dragger = new ImageButton(Icon.move, Styles.defaulti);
        add(dragger).uniformX().uniformY().fill();

        dragger.addListener(new InputListener() {

            public float dragx, dragy;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                dragx = x;
                dragy = y;
                isDragging = true;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                positionParent(x, y);
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
                isDragging = false;
            }

        });

        update(() -> {
            color.a = isDragging ? draggedAlpha : 1f;

            Vec2 pos = localToParentCoordinates(Tmp.v1.set(0, 0));
            setPosition(
                    Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
                    Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
            );
        });
    }

    public void positionParent(float x, float y) {
        if (parent == null) return;

        Vec2 pos = dragger.localToAscendantCoordinates(parent, Tmp.v1.set(x, y));
        setPosition(
                Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
                Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
        );
    }

}
