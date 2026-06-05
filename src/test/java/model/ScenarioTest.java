package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class ScenarioTest {

    private final String scenarioName = "Guten Morgen";
    private final String scenarioDescription = "Fährt die Rollläden hoch und kocht Kaffee";
    private Scenario scenario;
    @Mock
    private Action mockAction1;

    @Mock
    private Action mockAction2;

    @BeforeEach
    void setUp() {
        scenario = new Scenario(scenarioName, scenarioDescription);
    }

    @Test
    void testConstructorAndLombokGetters() {
        assertNotNull(scenario.getId());
        assertEquals(scenarioName, scenario.getName());
        assertEquals(scenarioDescription, scenario.getDescription());
        assertNotNull(scenario.getActions());
        assertTrue(scenario.getActions().isEmpty());
    }

    @Test
    void testLombokSetters() {
        UUID newId = UUID.randomUUID();
        scenario.setId(newId);
        assertEquals(newId, scenario.getId());

        scenario.setName("Gute Nacht");
        assertEquals("Gute Nacht", scenario.getName());

        scenario.setDescription("Alles ausschalten");
        assertEquals("Alles ausschalten", scenario.getDescription());

        List<Action> actionList = new ArrayList<>();
        actionList.add(mockAction1);
        scenario.setActions(actionList);
        assertEquals(actionList, scenario.getActions());
    }

    @Test
    void testAddAction() {
        scenario.addAction(mockAction1);

        assertEquals(1, scenario.getCount());
        assertTrue(scenario.getActions().contains(mockAction1));

        // Teste, ob Duplikate verhindert werden
        scenario.addAction(mockAction1);
        assertEquals(1, scenario.getCount(), "Aktion darf nicht doppelt hinzugefügt werden");
    }

    @Test
    void testRemoveAction() {
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        scenario.removeAction(mockAction1);

        assertEquals(1, scenario.getCount());
        assertFalse(scenario.getActions().contains(mockAction1));
        assertTrue(scenario.getActions().contains(mockAction2));
    }

    @Test
    void testExecute_CallsActionsInOrder() {
        // Arrange
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        // Act
        scenario.execute();

        // Assert: Pruefen, ob execute() in der richtigen Reihenfolge aufgerufen wird
        InOrder inOrder = inOrder(mockAction1, mockAction2);
        inOrder.verify(mockAction1).execute();
        inOrder.verify(mockAction2).execute();
    }

    @Test
    void testUndo_CallsActionsInReverseOrder() {
        // Arrange
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        // Act
        scenario.undo();

        // Assert: Pruefen, ob undo() in umgekehrter Reihenfolge (von hinten nach vorne) aufgerufen wird
        InOrder inOrder = inOrder(mockAction1, mockAction2);
        inOrder.verify(mockAction2).undo(); // Action 2 muss zuerst rueckgaengig gemacht werden
        inOrder.verify(mockAction1).undo(); // Dann Action 1
    }

    @Test
    void testToString() {
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        String expectedString = "Szenario: Guten Morgen (2 Aktionen)";
        assertEquals(expectedString, scenario.toString());
    }

//    @Test
//    void testGetDiscription() {
//        // Testet die manuell geschriebene Methode mit dem Tippfehler
//        assertEquals(scenarioDescription, scenario.getDiscription());
//    }

    @Test
    void testGetCount() {
        assertEquals(0, scenario.getCount());

        scenario.addAction(mockAction1);
        assertEquals(1, scenario.getCount());

        scenario.addAction(mockAction2);
        assertEquals(2, scenario.getCount());
    }
}