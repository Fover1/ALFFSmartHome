package model;

import controller.SmartHomeAppController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioActionTest {

    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream errStreamCaptor = new ByteArrayOutputStream();

    @Mock
    private Scenario mockScenario;

    @BeforeEach
    void setUp() {
        // Leitet System.err um, um die Konsolenausgabe bei undo() zu testen
        System.setErr(new PrintStream(errStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // Stellt System.err nach dem Test wieder her
        System.setErr(standardErr);
    }

    @Test
    void testConstructorsAndGetters() {
        // Testet den leeren Konstruktor (für GSON)
        ScenarioAction emptyAction = new ScenarioAction();
        assertNull(emptyAction.getTargetScenarioID());

        // Testet den UUID-Konstruktor
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);
        assertEquals(id, action.getTargetScenarioID());
        assertNull(action.getTargetScenario()); // Transient field sollte null sein
    }

    @Test
    void testExecute_ScenarioIsLoadedAndExecuted() {
        // Arrange
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        when(mockScenario.getId()).thenReturn(id);

        // Mit MockedConstruction fangen wir das "new SmartHomeAppController()" im Code ab
        // Jedes Mal, wenn die Klasse einen Controller erstellt, geben wir unseren praeparierten Mock zurueck
        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(List.of(mockScenario));
                })) {

            // Act
            action.execute();

            // Assert
            // Das Szenario muss aus dem Controller geladen und anschließend ausgefuehrt worden sein
            verify(mockScenario).execute();
            assertEquals(mockScenario, action.getTargetScenario());
        }
    }

    @Test
    void testExecute_ScenarioAlreadyLoaded() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        ScenarioAction action = new ScenarioAction(UUID.randomUUID());

        // Mit Reflection setzen wir das targetScenario, um zu simulieren, dass es schon geladen wurde
        Field scenarioField = ScenarioAction.class.getDeclaredField("targetScenario");
        scenarioField.setAccessible(true);
        scenarioField.set(action, mockScenario);

        // Act
        action.execute();

        // Assert
        verify(mockScenario).execute();
    }

    @Test
    void testUndo_ScenarioIsLoadedAndReverted() {
        // Arrange
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        when(mockScenario.getId()).thenReturn(id);

        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(List.of(mockScenario));
                })) {

            // Act
            action.undo();

            // Assert
            verify(mockScenario).undo();
            assertEquals(mockScenario, action.getTargetScenario());
        }
    }

    @Test
    void testUndo_ScenarioNotFoundPrintsError() {
        // Arrange
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        // Wir simulieren einen Controller, der eine leere Liste zurueckgibt (Szenario nicht gefunden)
        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(Collections.emptyList());
                })) {

            // Act
            action.undo();

            // Assert
            assertNull(action.getTargetScenario(), "Szenario sollte weiterhin null sein");

            // Pruefen, ob die Error-Nachricht auf System.err ausgegeben wurde
            String printedError = errStreamCaptor.toString().trim();
            assertTrue(printedError.length() > 0, "Es sollte eine Fehlermeldung ausgegeben werden");
            // Hinweis: Da wir ErrorMessages.TARGETSZENARIO_NOT_FOUND nicht direkt importiert haben,
            // pruefen wir hier einfach, ob überhaupt ein Fehler geloggt wurde.
        }
    }

    @Test
    void testGetDescription() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        ScenarioAction action = new ScenarioAction(UUID.randomUUID());

        when(mockScenario.getName()).thenReturn("Test Szenario");
        when(mockScenario.toString()).thenReturn("Szenario String Format");

        Field scenarioField = ScenarioAction.class.getDeclaredField("targetScenario");
        scenarioField.setAccessible(true);
        scenarioField.set(action, mockScenario);

        // Act
        String description = action.getDescription();

        // Assert
        assertEquals("Test Szenario: Szenario String Format", description);
    }
}