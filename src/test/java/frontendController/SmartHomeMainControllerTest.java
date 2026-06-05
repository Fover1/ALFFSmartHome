package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import model.LogEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmartHomeMainControllerTest {

    private SmartHomeMainController mainController;
    private StackPane contentArea;
    private ListView<LogEntry> logListView;
    private TextField time;
    private TextField deviceCount;

    @Mock
    private SmartHomeAppController mockAppController;

    @BeforeAll
    static void initToolkit() {
        //startet das JavaFX Toolkit
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits von anderen Tests gestartet wurde
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        mainController = new SmartHomeMainController();
        contentArea = new StackPane();
        logListView = new ListView<>();
        time = new TextField();
        deviceCount = new TextField();

        setPrivateField(mainController, "contentArea", contentArea);
        setPrivateField(mainController, "logListView", logListView);
        setPrivateField(mainController, "time", time);
        setPrivateField(mainController, "deviceCount", deviceCount);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSetControllerRegistersLogListener() {
        mainController.setController(mockAppController);
        verify(mockAppController).addLogListener(mainController);
    }

    @Test
    void testOnLogEntryCreatedAddsEntryToTop() {
        LogEntry oldEntry = new LogEntry(LocalDateTime.now(), "Szenario A", "Gerät A", "Aktion A", "Erfolg");
        logListView.getItems().add(oldEntry);

        LogEntry newEntry = new LogEntry(LocalDateTime.now(), "Szenario B", "Gerät B", "Aktion B", "Erfolg");

        mainController.onLogEntryCreated(newEntry);

        assertEquals(2, logListView.getItems().size(), "Das Logbuch sollte nun 2 Einträge haben");
        assertEquals(newEntry, logListView.getItems().get(0), "Der NEUE Eintrag muss ganz oben stehen");
        assertEquals(oldEntry, logListView.getItems().get(1), "Der ALTE Eintrag rutscht nach unten");
    }

    @Test
    void testHandleStepBackDelegatesToAppController() throws Exception {
        mainController.setController(mockAppController);

        Method method = SmartHomeMainController.class.getDeclaredMethod("handleStepBack");
        method.setAccessible(true);
        method.invoke(mainController);

        verify(mockAppController).undoLastAction();
    }
}