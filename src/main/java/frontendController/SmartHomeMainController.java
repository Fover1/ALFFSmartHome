package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class SmartHomeMainController {
    private SmartHomeAppController appController;

    @FXML
    private StackPane contentArea;


    public void setController(SmartHomeAppController appController) {
        this.appController = appController;
        System.out.println("Logik-Controller wurde erfolgreich an die GUI übergeben!");
        showDashboard();
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
            } else if (controller instanceof ScenarioControllerNew) {
                ((ScenarioControllerNew) controller).setAppController(this.appController);
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
}