import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class SkinnedAlert extends Alert {

    public SkinnedAlert(AlertType alertType) {
        super(alertType);
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("Hangman.css").toExternalForm());
    }
}
