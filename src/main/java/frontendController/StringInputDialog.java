package frontendController;

import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

import java.util.Optional;

public class StringInputDialog extends Dialog<String> {

    private final TextField textField;


    //privater Konstruktor, damit Klasse nicht mit NEW aufgerufen werden muss
    private StringInputDialog(String title, String header, String content, String defaultValue) {
        setTitle(title);
        setHeaderText(header);
        setContentText(content);

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        this.textField = new TextField(defaultValue);
        getDialogPane().setContent(textField);
        Platform.runLater(textField::requestFocus);

        setResultConverter(b -> (b == saveButtonType) ? textField.getText() : null);
    }

    public static Optional<String> get(String title, String header, String content, String defaultValue) {
        StringInputDialog dialog = new StringInputDialog(title, header, content, defaultValue);
        return dialog.showAndWait();
    }

    public static Optional<String> get(String title, String header, String content) {
        return get(title, header, content, "");
    }
}