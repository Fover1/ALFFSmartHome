package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import interfaces.Action;
import interfaces.SmartDevice;
import model.Scenario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScenarioControllerTest {

    private ScenarioController scenarioController;
    private TableView<Scenario> scenarioTable;
    private TableColumn<Scenario, String> colName;
    private TableColumn<Scenario, String> colDesc;
    private TableColumn<Scenario, Number> colActionCount;
    private VBox detailArea;
    private TextField txtName;
    private TextField txtDescription;
    private ListView<Action> actionListView;

    @Mock
    private SmartHomeAppController mockAppController;

    @BeforeAll
    static void initToolkit() {
        //startet das JavaFX Toolkit für die UI-Komponenten
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits läuft
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        scenarioController = new ScenarioController();

        scenarioTable = new TableView<>();
        colName = new TableColumn<>();
        colDesc = new TableColumn<>();
        colActionCount = new TableColumn<>();
        detailArea = new VBox();
        txtName = new TextField();
        txtDescription = new TextField();
        actionListView = new ListView<>();

        //Reflection Injections
        setPrivateField(scenarioController, "scenarioTable", scenarioTable);
        setPrivateField(scenarioController, "colName", colName);
        setPrivateField(scenarioController, "colDesc", colDesc);
        setPrivateField(scenarioController, "colActionCount", colActionCount);
        setPrivateField(scenarioController, "detailArea", detailArea);
        setPrivateField(scenarioController, "txtName", txtName);
        setPrivateField(scenarioController, "txtDescription", txtDescription);
        setPrivateField(scenarioController, "actionListView", actionListView);

        SmartDevice dummyDevice = mock(SmartDevice.class);
        Mockito.lenient().when(mockAppController.getAllDevices()).thenReturn(List.of(dummyDevice));

        Mockito.lenient().when(mockAppController.getAllScenarios()).thenReturn(new ArrayList<>());
        Mockito.lenient().when(mockAppController.getAllRooms()).thenReturn(new ArrayList<>());

        scenarioController.setAppController(mockAppController);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void invokePrivateMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    //fuehrt UI-Code sicher im JavaFX-Thread aus und faengt Exceptions ab
    private void runOnFxThread(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] fxError = new Throwable[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable e) {
                fxError[0] = e;
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        if (fxError[0] != null) {
            throw new Exception("Absturz im JavaFX-Thread: " + fxError[0].getMessage(), fxError[0]);
        }
    }

    @Test
    void testSetAppControllerPopulatesTable() {
        Scenario mockScenario = mock(Scenario.class);
        Mockito.when(mockAppController.getAllScenarios()).thenReturn(List.of(mockScenario));

        scenarioController.setAppController(mockAppController);

        assertEquals(1, scenarioTable.getItems().size());
        assertEquals(mockScenario, scenarioTable.getItems().get(0));
    }

    @Test
    void testShowScenarioDetailsWithSelection() {
        scenarioController.initialize();

        Scenario mockScenario = mock(Scenario.class);
        Mockito.when(mockScenario.getName()).thenReturn("Morgenroutine");
        Mockito.when(mockScenario.getDescription()).thenReturn("Fährt die Rollos hoch");

        Action mockAction = mock(Action.class);
        Mockito.when(mockScenario.getActions()).thenReturn(new ArrayList<>(List.of(mockAction)));

        scenarioTable.getItems().add(mockScenario);
        scenarioTable.getSelectionModel().select(mockScenario);

        assertFalse(detailArea.isDisable(), "Detail-Bereich muss aktiviert werden");
        assertEquals("Morgenroutine", txtName.getText());
        assertEquals("Fährt die Rollos hoch", txtDescription.getText());
        assertEquals(1, actionListView.getItems().size(), "Die Action-Liste muss gefüllt werden");
    }

    @Test
    void testHandleNewScenario() throws Exception {
        runOnFxThread(() -> {
            try {
                invokePrivateMethod(scenarioController, "handleNewScenario");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(mockAppController).addSzenario(any(Scenario.class));
        assertEquals(1, scenarioTable.getItems().size());

        Scenario createdScenario = scenarioTable.getItems().get(0);
        assertEquals(createdScenario, scenarioTable.getSelectionModel().getSelectedItem(),
                "Das neue Szenario muss sofort ausgewählt werden");
    }

    @Test
    void testHandleDeleteScenario() throws Exception {
        Scenario scenario = new Scenario("Test", "Test Desc");
        scenarioTable.getItems().add(scenario);
        scenarioTable.getSelectionModel().select(scenario);

        invokePrivateMethod(scenarioController, "handleDeleteScenario");

        verify(mockAppController).removeScenario(scenario);
        verify(mockAppController).save();
        assertTrue(scenarioTable.getItems().isEmpty(), "Tabelle muss nach dem Löschen leer sein");
    }

    @Test
    void testHandleSaveScenarioDetails() throws Exception {
        Scenario scenario = mock(Scenario.class);
        scenarioTable.getItems().add(scenario);
        scenarioTable.getSelectionModel().select(scenario);

        txtName.setText("Neuer Name");
        txtDescription.setText("Neue Beschreibung");

        invokePrivateMethod(scenarioController, "handleSaveScenarioDetails");

        verify(scenario).setName("Neuer Name");
        verify(scenario).setDescription("Neue Beschreibung");
        verify(mockAppController).save();
    }

    @Test
    void testHandleMoveActionUp() throws Exception {
        Scenario scenario = mock(Scenario.class);
        Action action1 = mock(Action.class);
        Action action2 = mock(Action.class);

        List<Action> actions = new ArrayList<>(List.of(action1, action2));
        Mockito.when(scenario.getActions()).thenReturn(actions);

        scenarioTable.getItems().add(scenario);
        scenarioTable.getSelectionModel().select(scenario);

        actionListView.getItems().addAll(actions);
        actionListView.getSelectionModel().select(1);

        invokePrivateMethod(scenarioController, "handleMoveActionUp");

        verify(mockAppController).save();
        assertEquals(action2, actions.get(0));
        assertEquals(action1, actions.get(1));
    }
}