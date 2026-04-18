package model;

import devices.Light;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/// todo: das ist ein generierter Test. Er musste zwar noch ein wenig angepasst werden, damit er funktioniert, er sollte aber nochmal genau geprüft werden :)

class AbstractDeviceTest {

    private Light device;
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        mockRoom = mock(Room.class);
        device = new Light("device-123", "Test Lampe", mockRoom);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("device-123", device.getId());
        assertEquals("Test Lampe", device.getName());
        assertEquals(mockRoom, device.getRoom());
        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
    }

    @Test
    void testSetters() {
        Room newRoom = mock(Room.class);
        device.setName("Neue Lampe");
        device.setRoom(newRoom);

        assertEquals("Neue Lampe", device.getName());
        assertEquals(newRoom, device.getRoom());
    }

    @Test
    void testRestoreAfterLoad() {
        // Wir machen die Listen absichtlich null/leer, um den Restore-Prozess zu testen
        device.setObservers(null);
        device.setFunctions(null);

        device.restoreAfterLoad();

        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
        // initializeFunctions() sollte aufgerufen worden sein
        assertTrue(device.getAvailableFunctions().contains("Schalten"));
        assertTrue(device.getAvailableFunctions().contains("Helligkeit"));


    }

    @Test
    void testExecuteFunctionSuccess() {
        // Erstelle einen Mock für die Funktion und den Observer
        DeviceFunction mockFunction = mock(DeviceFunction.class);
        DeviceObserver mockObserver = mock(DeviceObserver.class);

        device.getFunctions().put("mockedFunction", mockFunction);
        device.addObserver(mockObserver);

        // Ausführen
        device.executeFunction("mockedFunction", "paramValue");

        // Verifizieren, dass die Funktion ausgeführt und der Observer benachrichtigt wurde
        verify(mockFunction, times(1)).execute("paramValue");
        verify(mockObserver, times(1)).onStateChanged(device);
    }

    @Test
    void testExecuteFunctionThrowsExceptionIfNotFound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> device.executeFunction("nonExistentFunction", null)
        );
        assertTrue(exception.getMessage().contains("nonExistentFunction"));
    }

    @Test
    void testGetAvailableFunctions() {
        List<String> available = device.getAvailableFunctions();
        assertEquals(2, available.size());
        assertTrue(device.getAvailableFunctions().contains("Schalten"));
        assertTrue(device.getAvailableFunctions().contains("Helligkeit"));
    }

    @Test
    void testAddAndRemoveObserver() {
        DeviceObserver observer = mock(DeviceObserver.class);

        // Test Add
        device.addObserver(observer);
        assertEquals(1, device.getObservers().size());

        // Test doppeltes Hinzufügen (sollte ignoriert werden)
        device.addObserver(observer);
        assertEquals(1, device.getObservers().size());

        // Test Remove
        device.removeObserver(observer);
        assertEquals(0, device.getObservers().size());
    }

    @Test
    void testAddObserverWhenListIsNull() {
        // Simuliere den Fall, dass die Liste null ist (Edge Case)
        device.setObservers(null);
        DeviceObserver observer = mock(DeviceObserver.class);

        device.addObserver(observer);
        assertEquals(1, device.getObservers().size());
    }

    @Test
    void testRemoveObserverWhenListIsNull() {
        // Simuliere den Fall, dass die Liste null ist (Edge Case)
        device.setObservers(null);
        DeviceObserver observer = mock(DeviceObserver.class);

        // Sollte keine NullPointerException werfen
        assertDoesNotThrow(() -> device.removeObserver(observer));
    }

    @Test
    void testNotifyObserversWhenListIsNull() {
        // Simuliere den Fall, dass die Liste null ist (Edge Case)
        device.setObservers(null);

        // Sollte keine NullPointerException werfen
        assertDoesNotThrow(() -> device.executeFunction("Schalten", null));
    }
}