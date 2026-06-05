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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartHomeModelTest {

    private SmartHomeModel model;

    @Mock
    private Room mockRoom1;

    @Mock
    private Room mockRoom2;

    @Mock
    private SmartDevice mockDevice1;

    @Mock
    private SmartDevice mockDevice2;

    @Mock
    private Scenario mockScenario;

    @BeforeEach
    void setUp() {
        model = new SmartHomeModel();
    }

    @Test
    void testConstructorAndLombokGetters() {
        assertNotNull(model.getRooms(), "Räume-Liste sollte nicht null sein");
        assertTrue(model.getRooms().isEmpty(), "Räume-Liste sollte initial leer sein");

        assertNotNull(model.getScenarios(), "Szenarien-Liste sollte nicht null sein");
        assertTrue(model.getScenarios().isEmpty(), "Szenarien-Liste sollte initial leer sein");
    }

    @Test
    void testLombokSetters() {
        List<Room> newRooms = new ArrayList<>();
        newRooms.add(mockRoom1);
        model.setRooms(newRooms);
        assertEquals(newRooms, model.getRooms());

        List<Scenario> newScenarios = new ArrayList<>();
        newScenarios.add(mockScenario);
        model.setScenarios(newScenarios);
        assertEquals(newScenarios, model.getScenarios());
    }

    @Test
    void testAddAndRemoveRoom() {
        // Hinzufuegen
        model.addRoom(mockRoom1);
        assertEquals(1, model.getRooms().size());
        assertTrue(model.getRooms().contains(mockRoom1));

        // Entfernen
        model.removeRoom(mockRoom1);
        assertTrue(model.getRooms().isEmpty());
    }

    @Test
    void testChangeRoomName() {
        String newName = "Küche";
        model.changeRoomName(mockRoom1, newName);

        // Das Model sollte einfach setName auf dem Raum aufrufen
        verify(mockRoom1).setName(newName);
    }

    @Test
    void testAddAndRemoveDevice() {
        // Test addDevice (Delegation an den Raum)
        model.addDevice(mockRoom1, mockDevice1);
        verify(mockRoom1).addDevice(mockDevice1);

        // Test removeDevice (Delegation an den Raum)
        model.removeDevice(mockDevice1, mockRoom1);
        verify(mockRoom1).removeDevice(mockDevice1);
    }

    @Test
    void testChangeDeviceRoom() {
        // Act: Verschiebt das Geraet von Raum 1 in Raum 2
        model.changeDeviceRoom(mockDevice1, mockRoom1, mockRoom2);

        // Assert: Es muss aus Raum 1 entfernt und zu Raum 2 hinzugefuegt worden sein
        verify(mockRoom1).removeDevice(mockDevice1);
        verify(mockRoom2).addDevice(mockDevice1);
    }

    @Test
    void testAddAndRemoveScenario() {
        // Hinzufuegen
        model.addScenario(mockScenario);
        assertEquals(1, model.getScenarios().size());
        assertTrue(model.getScenarios().contains(mockScenario));

        // Entfernen
        model.removeScenario(mockScenario);
        assertTrue(model.getScenarios().isEmpty());
    }

    @Test
    void testGetAllDevices() {
        // Arrange
        // Wir fuegen dem Model zwei Raeume hinzu
        model.addRoom(mockRoom1);
        model.addRoom(mockRoom2);

        // Raum 1 enthaelt Geraet 1, Raum 2 enthaelt Geraet 2
        when(mockRoom1.getSmartDevices()).thenReturn(List.of(mockDevice1));
        when(mockRoom2.getSmartDevices()).thenReturn(List.of(mockDevice2));

        // Act
        List<SmartDevice> allDevices = model.getAllDevices();

        // Assert
        // Das Model muss eine kombinierte Liste aus allen Raeumen zurueckgeben
        assertEquals(2, allDevices.size());
        assertTrue(allDevices.contains(mockDevice1));
        assertTrue(allDevices.contains(mockDevice2));
    }

    @Test
    void testGetAllDevices_EmptyRooms() {
        // Arrange: Raum existiert, aber hat keine Geraete
        model.addRoom(mockRoom1);
        when(mockRoom1.getSmartDevices()).thenReturn(new ArrayList<>());

        // Act
        List<SmartDevice> allDevices = model.getAllDevices();

        // Assert
        assertTrue(allDevices.isEmpty());
    }

    @Test
    void testChangeDeviceName() {
        String newDeviceName = "Stehlampe";
        model.changeDeviceName(mockDevice1, newDeviceName);

        // Das Model sollte einfach setName auf dem Geraet aufrufen
        verify(mockDevice1).setName(newDeviceName);
    }
}