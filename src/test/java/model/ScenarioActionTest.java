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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        //leitet System.err um, um die Konsolenausgabe bei undo() zu testen
        System.setErr(new PrintStream(errStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        //stellt System.err nach dem Test wieder her
        System.setErr(standardErr);
    }

    @Test
    void testConstructorsAndGetters() {
        ScenarioAction emptyAction = new ScenarioAction();
        assertNull(emptyAction.getTargetScenarioID());

        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);
        assertEquals(id, action.getTargetScenarioID());
        assertNull(action.getTargetScenario());
    }

    @Test
    void testExecuteScenarioIsLoadedAndExecuted() {
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        when(mockScenario.getId()).thenReturn(id);
        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(List.of(mockScenario));
                })) {
            action.execute();

            verify(mockScenario).execute();
            assertEquals(mockScenario, action.getTargetScenario());
        }
    }

    @Test
    void testExecuteScenarioAlreadyLoaded() throws NoSuchFieldException, IllegalAccessException {
        ScenarioAction action = new ScenarioAction(UUID.randomUUID());

        //mit Reflection wird targetScenario gesetzt, um zu simulieren, dass es schon geladen wurde
        Field scenarioField = ScenarioAction.class.getDeclaredField("targetScenario");
        scenarioField.setAccessible(true);
        scenarioField.set(action, mockScenario);
        action.execute();

        verify(mockScenario).execute();
    }

    @Test
    void testUndoScenarioIsLoadedAndReverted() {
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        when(mockScenario.getId()).thenReturn(id);

        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(List.of(mockScenario));
                })) {

            action.undo();

            verify(mockScenario).undo();
            assertEquals(mockScenario, action.getTargetScenario());
        }
    }

    @Test
    void testUndoScenarioNotFoundPrintsError() {
        UUID id = UUID.randomUUID();
        ScenarioAction action = new ScenarioAction(id);

        try (MockedConstruction<SmartHomeAppController> mockedController = mockConstruction(
                SmartHomeAppController.class,
                (mockControllerInstance, context) -> {
                    when(mockControllerInstance.getAllScenarios()).thenReturn(Collections.emptyList());
                })) {

            action.undo();

            assertNull(action.getTargetScenario(), "Szenario sollte weiterhin null sein");
            String printedError = errStreamCaptor.toString().trim();
            assertFalse(printedError.isEmpty(), "Es sollte eine Fehlermeldung ausgegeben werden");
        }
    }

    @Test
    void testGetDescription() throws NoSuchFieldException, IllegalAccessException {
        ScenarioAction action = new ScenarioAction(UUID.randomUUID());

        when(mockScenario.getName()).thenReturn("Test Szenario");
        when(mockScenario.toString()).thenReturn("Szenario String Format");

        Field scenarioField = ScenarioAction.class.getDeclaredField("targetScenario");
        scenarioField.setAccessible(true);
        scenarioField.set(action, mockScenario);

        String description = action.getDescription();

        assertEquals("Test Szenario: Szenario String Format", description);
    }
}