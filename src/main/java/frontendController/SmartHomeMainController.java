package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import model.LogEntry;
import model.LogListener;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SmartHomeMainController implements LogListener {
    private SmartHomeAppController appController;

    @FXML
    private StackPane contentArea;

    @FXML
    private ListView<LogEntry> logListView;

    @FXML
    private ListView<LogEntry> logListView1;

    public void setController(SmartHomeAppController appController) {
        this.appController = appController;
        System.out.println("Logik-Controller wurde erfolgreich an die GUI übergeben!");
        this.appController.addLogListener(this);
        showDashboard();
    }

    @Override
    public void onLogEntryCreated(LogEntry entry) {
        logListView.getItems().addFirst(entry);
        logListView1.getItems().addFirst(entry);
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);

            Object controller = loader.getController();

            // Wenn es der RoomController ist, geben wir ihm den AppController (bei deviceView kann der RoomController den appController weitergeben)
            if (controller instanceof RoomController) {
                ((RoomController) controller).setAppController(this.appController);
            } else if (controller instanceof ScenarioController) {
                ((ScenarioController) controller).setAppController(this.appController);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Laden der Datei: " + fxmlFile);
        }
    }

    @FXML
    public void showDashboard() {
        loadView("DashboardView.fxml");
    }

    @FXML
    public void showRooms() {
        loadView("RoomView.fxml");
    }

    @FXML
    private void showScenarios() {
        loadView("ScenarioView.fxml");
    }

    @FXML
    private void handleStepBack() {
        /// todo: implement
    }

    @FXML
    private void handleOpenConfig() {
        /// todo: hier muss noch etwas implementiert werden, um das mopped auszuwählen
        String filename = "testconfig.json";
        openConfig(filename);
    }

    /// todo: wollen wir hier den Namen oder die ganze Datei mit übergeben?
    private void openConfig(String filename) {
        appController.loadConfiguration(filename);
        showDashboard();
    }

    @FXML
    private void handleCreateNewConfig() {
        TextInputDialog dialog = new TextInputDialog("was das hier?");
        dialog.setTitle("Neue Konfiguration erstellen");
        dialog.setHeaderText("Geben Sie den Namen der neuen Konfigurationsdatei ein:");
        dialog.setContentText("Dateiname:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(fileName -> {
            if (!fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json";
            }
            File newFile = new File(System.getProperty("user.dir"), fileName);
            openConfig(newFile.getName());
        });
    }
}