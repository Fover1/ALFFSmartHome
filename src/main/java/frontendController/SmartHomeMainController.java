package frontendController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import model.LogEntry;
import model.LogListener;
import model.PersistenceManager;
import model.Room;
import model.Scenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmartHomeMainController implements LogListener {
    private SmartHomeAppController appController;

    @FXML
    private StackPane contentArea;

    @FXML
    private ListView<LogEntry> logListView;

    @FXML
    private ListView<LogEntry> logListView1;

    @FXML
    private Label hausanzeige;

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
            PersistenceManager persistenceManager = new PersistenceManager();
            hausanzeige.setText(persistenceManager.getFileName().substring(0, persistenceManager.getFileName().lastIndexOf(".")));
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
        File configDirectory = new File(System.getProperty("user.dir"));
        File[] jsonFiles = configDirectory.listFiles((dir, name) -> name.endsWith(".json"));

        List<String> options = new ArrayList<>();
        if (jsonFiles != null) {
            for (File file : jsonFiles) {
                options.add(file.getName());
            }
        }

        if (options.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Keine Dateien gefunden");
            alert.setHeaderText(null);
            alert.setContentText("Im Ordner '" + configDirectory.getAbsolutePath() + "' wurden keine Konfigurationen gefunden.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Konfiguration laden");
        dialog.setHeaderText("Wählen Sie eine vorhandene Konfiguration aus.");
        dialog.setContentText("Datei:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selected -> {
            openConfig(selected);
            System.out.println("Datei wird geladen in handleOpenConfig: " + selected);
        });

    }

    /// todo: wollen wir hier den Namen oder die ganze Datei mit übergeben?
    private void openConfig(String filename) {

        appController.loadConfiguration(filename);
        System.out.println("Datei wird geladen in openConfig: " + filename);
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
            safeNewFile(fileName);
            File newFile = new File(System.getProperty("user.dir"), fileName);
            System.out.println("neue datei wurde erstellt");
            System.out.println(System.getProperty("user.dir"));
            openConfig(fileName);
        });
    }

    private void safeNewFile(String filePath) {
        try (FileWriter fw = new FileWriter(filePath)) {
            PersistenceManager.SmartHomeData emptyData = new PersistenceManager.SmartHomeData(new ArrayList<Room>(), new ArrayList<Scenario>());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(emptyData, fw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}