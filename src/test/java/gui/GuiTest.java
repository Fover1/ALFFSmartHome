package gui;

import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.example.SmartHomeApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GuiTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new SmartHomeApp().start(stage);
    }

    @BeforeEach
    void loadConfiguration() {
        // Laedt die "smarthome_config.json" fuer den GUI-test
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".list-cell");
        clickOn("smarthome_config.json");
        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void roomViewContainsRooms() {
        // In der Raumuebersicht werden Raeume angezeigt
        clickOn("Räume");
        WaitForAsyncUtils.waitForFxEvents();
        FlowPane roomContainer =
                lookup("#roomContainer").queryAs(FlowPane.class);
        assertNotNull(roomContainer);
        assertFalse(
                roomContainer.getChildren().isEmpty(),
                "Es sollten Räume angezeigt werden."
        );
    }

    @Test
    void createScenarioAddsTableEntry() {
        // Hinzufuegen eines Szenarios erweitert die Szenario-Tabelle
        clickOn("Szenarien");
        WaitForAsyncUtils.waitForFxEvents();
        TableView<?> scenarioTable =
                lookup("#scenarioTable").queryTableView();
        int before = scenarioTable.getItems().size();
        clickOn("Neu");
        WaitForAsyncUtils.waitForFxEvents();
        int after = scenarioTable.getItems().size();
        assertEquals(
                before + 1,
                after,
                "Nach dem Klick auf 'Neu' sollte ein Szenario angelegt werden."
        );
    }

    @Test
    // Hierfuer muss in der smarthome_config.json in dem obersten Szenario der Tabelle eine ausfuehrbare Action sein
    void executingScenarioCreatesLogEntry() {
        // Ausfueheren des ersten Szenarios der Szenario-Tabelle erstellt einen Logeintrag
        clickOn("Szenarien");
        WaitForAsyncUtils.waitForFxEvents();
        TableView<?> scenarioTable =
                lookup("#scenarioTable").queryTableView();
        assertFalse(
                scenarioTable.getItems().isEmpty(),
                "Für diesen Test muss mindestens ein Szenario vorhanden sein."
        );
        interact(() ->
                scenarioTable.getSelectionModel().selectFirst());
        ListView<?> logView =
                lookup("#logListView").queryListView();
        int before = logView.getItems().size();
        clickOn("Ausführen");
        WaitForAsyncUtils.waitForFxEvents();
        int after = logView.getItems().size();
        assertTrue(
                after > before,
                "Durch die Ausführung des Szenarios sollte ein Logeintrag erzeugt werden."
        );
    }
}