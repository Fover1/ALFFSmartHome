package model;

import interfaces.DeviceFunction;
import interfaces.DeviceObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AbstractDeviceTest {

    private DummyDevice device;
    private final UUID testId = UUID.randomUUID();
    private final String testName = "Test Gerät";

    @Mock
    private DeviceObserver mockObserver;

    @Mock
    private DeviceFunction mockFunction;

    // Wir erstellen eine konkrete Subklasse nur für diesen Test
    class DummyDevice extends AbstractDevice {

        public DummyDevice(UUID id, String name) {
            super(id, name);
        }

        @Override
        protected void initializeFunctions() {
            // Wir legen hier unsere gemockte Funktion in die Map,
            // damit wir später prüfen können, ob sie ausgeführt wird.
            this.functions.put("TestFunction", mockFunction);
        }

        @Override
        public String getDeviceType() {
            return "DummyType";
        }

        @Override
        public String getCurrentState() {
            return "DummyState";
        }
    }

    @BeforeEach
    void setUp() {
        // Bei der Instanziierung wird der Konstruktor von AbstractDevice aufgerufen,
        // welcher restoreAfterLoad() und damit initializeFunctions() ausführt.
        device = new DummyDevice(testId, testName);
    }

    @Test
    void testConstructorAndLombokGetters() {
        assertEquals(testId, device.getId());
        assertEquals(testName, device.getName());
        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
    }

    @Test
    void testLombokSetters() {
        device.setName("Neuer Name");
        assertEquals("Neuer Name", device.getName());

        List<DeviceObserver> newObservers = new ArrayList<>();
        device.setObservers(newObservers);
        assertEquals(newObservers, device.getObservers());
    }

    @Test
    void testRestoreAfterLoad_NullLists() {
        // Simuliere Zustand nach JSON-Deserialisierung (Listen sind null)
        device.setObservers(null);
        device.setFunctions(null);

        device.restoreAfterLoad();

        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
        assertTrue(device.getFunctions().containsKey("TestFunction"));
    }

    @Test
    void testRestoreAfterLoad_EmptyFunctions() {
        // Simuliere leere Funktionsliste
        device.getFunctions().clear();

        device.restoreAfterLoad();

        // initializeFunctions() sollte aufgerufen worden sein
        assertTrue(device.getFunctions().containsKey("TestFunction"));
    }

    @Test
    void testAddAndRemoveObserver() {
        device.addObserver(mockObserver);
        assertTrue(device.getObservers().contains(mockObserver));

        // Doppeltes Hinzufügen testen (sollte verhindert werden)
        device.addObserver(mockObserver);
        assertEquals(1, device.getObservers().size());

        device.removeObserver(mockObserver);
        assertFalse(device.getObservers().contains(mockObserver));
    }

    @Test
    void testAddObserver_NullList() {
        device.setObservers(null);
        device.addObserver(mockObserver);

        assertNotNull(device.getObservers());
        assertTrue(device.getObservers().contains(mockObserver));
    }

    @Test
    void testNotifyObservers() {
        device.addObserver(mockObserver);

        device.notifyObservers();

        // Überprüft, ob das Gerät dem Observer gemeldet hat, dass sich sein Status geändert hat
        verify(mockObserver).onStateChanged(device);
    }

    @Test
    void testNotifyObservers_NullList() {
        device.setObservers(null);
        // Darf keine NullPointerException werfen
        device.notifyObservers();
        verifyNoInteractions(mockObserver);
    }

    @Test
    void testGetAvailableFunctions() {
        List<String> functions = device.getAvailableFunctions();
        assertEquals(1, functions.size());
        assertTrue(functions.contains("TestFunction"));
    }

    @Test
    void testGetFunction() {
        DeviceFunction function = device.getFunction("TestFunction");
        assertEquals(mockFunction, function);

        assertNull(device.getFunction("NichtVorhanden"));
    }

    @Test
    void testExecuteFunction_Success() {
        Object param = 42;
        device.addObserver(mockObserver);

        device.executeFunction("TestFunction", param);

        // Prüfen, ob die Funktion mit dem richtigen Parameter ausgeführt wurde
        verify(mockFunction).execute(param);

        // Nach der Ausführung müssen die Observer benachrichtigt werden
        verify(mockObserver).onStateChanged(device);
    }

    @Test
    void testExecuteFunction_FunctionNotFoundThrowsException() {
        // Assertions werfen eine Exception, wenn die Funktion nicht existiert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            device.executeFunction("UnbekannteFunktion", null);
        });

        // Optional: Die Exception-Message prüfen (hängt von ErrorMessages.FUNCTION_NOT_FOUND ab)
        assertTrue(exception.getMessage().contains("UnbekannteFunktion"));
    }

    @Test
    void testAbstractMethods() {
        // Ruft die in der DummyDevice implementierten abstrakten Methoden auf
        assertEquals("DummyType", device.getDeviceType());
        assertEquals("DummyState", device.getCurrentState());
    }
}