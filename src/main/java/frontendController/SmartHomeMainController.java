package frontendController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.SmartHomeAppController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.LogEntry;
import model.LogListener;
import model.PersistenceManager;
import model.Room;
import model.Scenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmartHomeMainController implements LogListener {
    private static final String CONFIG_FOLDER = "configurations";
    private SmartHomeAppController appController;
    @FXML
    private StackPane contentArea;

    @FXML
    private ListView<LogEntry> logListView;

    @FXML
    private ListView<LogEntry> logListView1;

    @FXML
    private TextField time;

    @FXML
    private TextField deviceCount;

    public void setController(SmartHomeAppController appController) {
        this.appController = appController;
        this.appController.addLogListener(this);
        //Hauptfenster wird schon gezeichnet, wenn das Popup kommt
        Platform.runLater(this::handleOpenConfig);
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
        appController.undoLastAction();
    }

    @FXML
    private void handleOpenConfig() {
        File configDirectory = new File(CONFIG_FOLDER);
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
            alert.setContentText("Im Ordner '" + configDirectory.getAbsolutePath() + "' wurden keine Konfigurationen gefunden. \n Sie werden zur Konfigurationserstellung weitergeleitet");
            alert.showAndWait();
            handleCreateNewConfig();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Konfiguration laden");
        dialog.setHeaderText("Wählen Sie eine vorhandene Konfiguration aus.");
        dialog.setContentText("Datei:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selected -> {
            openConfig(CONFIG_FOLDER + "/" + selected);
            System.out.println("Datei wird geladen in handleOpenConfig: " + selected);
        });

    }

    //Problem: In JAva FX gibt es nur einen Thread. Daher kann man diesem nicht einfach sagen, aktualisiere durchgehend die Uhr
    //Daher gibt es die Timeline. Dieser kann man sagen, führen folgenden Code immer nach so und so viel Zeit aus.
    //Dieser geht dann zum Thread, führt den Code aus und wartet dann wieder eine bestimmte Zeit im Hintergrund
    @FXML
    public void initialize() {
        // Eine Timeline erstellen, die jede Sekunde ausgelöst wird
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            //Uhrzeit aktualisieren
            LocalTime currentTime = LocalTime.now();
            time.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            //Geräteanzahl aktualisieren
            if (appController != null) {
                int totalDevices = appController.getAllDevices().size();
                deviceCount.setText(totalDevices + " Geräte");
            }
        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

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

            String path = CONFIG_FOLDER + "/" + fileName;
            safeNewFile(path);
            openConfig(path);
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