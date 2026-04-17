package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ScenarioTest {

    private Scenario scenario;
    private Action mockAction1;
    private Action mockAction2;

    @BeforeEach
    void setUp() {
        scenario = new Scenario("Abendmodus", "Schaltet Lichter an und Heizung aus");
        mockAction1 = mock(Action.class);
        mockAction2 = mock(Action.class);
    }

    @Test
    void testAddAction() {
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        assertEquals(2, scenario.getCount());
    }

//    @Test
//    void testAddDuplicateAction() {
//        scenario.addAction(mockAction1);
//        scenario.addAction(mockAction1); // Versuch, das gleiche Objekt nochmal zu adden
//
//        assertEquals(1, scenario.getCount(), "Duplikate sollten verhindert werden");
//    }

    @Test
    void testRemoveAction() {
        scenario.addAction(mockAction1);
        scenario.removeAction(mockAction1);

        assertEquals(0, scenario.getCount());
    }

    @Test
    void testExecute() {
        scenario.addAction(mockAction1);
        scenario.addAction(mockAction2);

        scenario.execute();

        verify(mockAction1, times(1)).execute();
        verify(mockAction2, times(1)).execute();
    }

    @Test
    void testGetDescription() {
        scenario.addAction(mockAction1);

        String description = scenario.getDescription();

        assertEquals("Szenario: Abendmodus (1 Aktionen)", description);
    }

    @Test
    void testExecuteEmptyScenario() {
        assertDoesNotThrow(() -> scenario.execute());
    }
}