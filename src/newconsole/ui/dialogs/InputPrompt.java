package newconsole.ui.dialogs;

import arc.func.*;
import arc.scene.ui.*;
import mindustry.*;
import mindustry.ui.*;

/**
 * A text input dialog.
 * @author Mnemotechnician
 */
@SuppressWarnings("CanBeFinal")
public class InputPrompt extends Dialog {

    public Label label;
    public TextField field;

    protected Cons<String> onFinish;

    public InputPrompt() {
        super("");
        closeOnBack();
        cont.center();

        label = new Label("");
        label.setWrap(true);
        cont.add(label).growX().row();

        field = cont.field("", (field, letter) -> {
            if (letter == '\n') done();
            return true;
        }, text -> {
        }).width(200f).get();
        field.removeInputDialog();

        cont.button("@newconsole.done", Styles.defaultt, this::done).width(80f).row();

        cont.button("@newconsole.close", Styles.defaultt, this::hide).colspan(2).growX();
    }

    /**
     * Shows the dialog, runs the consumer when the done button is pressed
     */
    public void prompt(String title, String defaultText, Cons<String> cons) {
        this.onFinish = cons;

        label.setText(title == null ? "" : title);
        field.setText(defaultText);

        show();
    }

    public void prompt(String title, Cons<String> cons) {
        prompt(title, "", cons);
    }

    protected void done() {
        if (onFinish != null) {
            String text = field.getText();
            if (text.replaceAll("\\s", "").isEmpty()) {
                Vars.ui.showInfo("@newconsole.empty-field");
            } else {
                onFinish.get(text);
                onFinish = null;
            }
        }
        hide();
    }

}
