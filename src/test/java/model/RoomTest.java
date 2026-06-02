package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoomTest {

    private Room room;
    private final String roomName = "Wohnzimmer";

    @Mock
    private SmartDevice mockDevice;

    @Mock
    private RoomObserver mockObserver;

    @BeforeEach
    void setUp() {
        room = new Room(roomName);
    }

    @Test
    void testConstructorAndLombokGettersSetters() {
        // Assert Constructor
        assertEquals(roomName, room.getName());
        assertNotNull(room.getSmartDevices());
        assertNotNull(room.getRoomObservers());
        assertTrue(room.getSmartDevices().isEmpty());

        // Test Setters
        room.setName("Schlafzimmer");
        assertEquals("Schlafzimmer", room.getName());

        List<SmartDevice> devices = new ArrayList<>();
        devices.add(mockDevice);
        room.setSmartDevices(devices);
        assertEquals(devices, room.getSmartDevices());
    }

    @Test
    void testAddDevice() {
        room.addObserver(mockObserver);

        // Act
        room.addDevice(mockDevice);

        // Assert
        assertTrue(room.getSmartDevices().contains(mockDevice));
        assertEquals(1, room.getSmartDevices().size());

        // Prüfen, ob der Observer benachrichtigt wurde
        verify(mockObserver, times(1)).onDeviceListChanged(room);
    }

    @Test
    void testAddDevice_DuplicateDevice() {
        room.addObserver(mockObserver);
        room.addDevice(mockDevice); // Erstes Hinzufügen (notify = 1)

        // Act: Versuche dasselbe Gerät nochmal hinzuzufügen
        room.addDevice(mockDevice);

        // Assert
        assertEquals(1, room.getSmartDevices().size(), "Gerät darf nicht doppelt in der Liste sein");

        // notifyObservers() darf bei Duplikaten nicht nochmal aufgerufen werden
        verify(mockObserver, times(1)).onDeviceListChanged(room);
    }

    @Test
    void testRemoveDevice() {
        room.addDevice(mockDevice);
        room.addObserver(mockObserver);

        // Act
        room.removeDevice(mockDevice);

        // Assert
        assertFalse(room.getSmartDevices().contains(mockDevice));
        verify(mockObserver, times(1)).onDeviceListChanged(room);
    }

    @Test
    void testAddObserver() {
        room.addObserver(mockObserver);
        assertTrue(room.getRoomObservers().contains(mockObserver));

        // Doppeltes Hinzufügen testen
        room.addObserver(mockObserver);
        assertEquals(1, room.getRoomObservers().size());
    }

    @Test
    void testAddObserver_NullList() {
        // Simuliere Zustand nach JSON Deserialisierung (transient List = null)
        room.setRoomObservers(null);

        // Act
        room.addObserver(mockObserver);

        // Assert
        assertNotNull(room.getRoomObservers(), "Die Liste sollte neu initialisiert werden");
        assertTrue(room.getRoomObservers().contains(mockObserver));
    }

    @Test
    void testRemoveObserver() {
        room.addObserver(mockObserver);

        // Act
        room.removeObserver(mockObserver);

        // Assert
        assertFalse(room.getRoomObservers().contains(mockObserver));
    }

    @Test
    void testRemoveObserver_NullList() {
        room.setRoomObservers(null);

        // Darf keine NullPointerException werfen
        room.removeObserver(mockObserver);

        assertNull(room.getRoomObservers());
    }

    @Test
    void testNotifyObservers_NullList() {
        room.setRoomObservers(null);

        // addDevice löst notifyObservers() aus.
        // Wenn die Liste null ist, darf das Programm nicht abstürzen.
        room.addDevice(mockDevice);

        // Da die Liste null war und der Observer nie drin war, passiert logischerweise nichts
        verify(mockObserver, never()).onDeviceListChanged(room);
    }
}