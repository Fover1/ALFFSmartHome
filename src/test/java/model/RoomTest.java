package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;


class RoomTest {

    private Room room;
    private Device mockDevice1;
    private Device mockDevice2;

    @BeforeEach
    void setUp() {
        // Wird vor jedem Test ausgeführt, um einen sauberen Zustand zu haben
        room = new Room("Wohnzimmer");
        mockDevice1 = mock(Device.class);
        mockDevice2 = mock(Device.class);
    }

    @Test
    void constructorShouldSetNameAndInitializeEmptyList() {
        assertEquals("Wohnzimmer", room.getName(), "Der Name des Raums sollte korrekt gesetzt werden.");
        assertNotNull(room.getDevices(), "Die Geräteliste sollte nicht null sein.");
        assertTrue(room.getDevices().isEmpty(), "Die Geräteliste sollte anfangs leer sein.");
    }

    @Test
    void setNameShouldUpdateName() {
        room.setName("Schlafzimmer");
        assertEquals("Schlafzimmer", room.getName(), "Der Setter sollte den Namen des Raums aktualisieren.");
    }

    @Test
    void setDevicesShouldUpdateDeviceList() {
        List<Device> newList = new ArrayList<>();
        newList.add(mockDevice1);

        room.setDevices(newList);

        assertEquals(1, room.getDevices().size());
        assertTrue(room.getDevices().contains(mockDevice1));
    }

    @Test
    void addDeviceShouldAddDeviceIfNotPresent() {
        room.addDevice(mockDevice1);

        assertEquals(1, room.getDevices().size(), "Die Liste sollte genau ein Gerät enthalten.");
        assertTrue(room.getDevices().contains(mockDevice1), "Das hinzugefügte Gerät sollte in der Liste sein.");
    }

    @Test
    void addDeviceShouldNotAddDuplicateDevice() {
        room.addDevice(mockDevice1);
        room.addDevice(mockDevice1); // Versuch, dasselbe Gerät noch einmal hinzuzufügen

        assertEquals(1, room.getDevices().size(), "Duplikate sollten nicht hinzugefügt werden.");
    }

    @Test
    void removeDeviceShouldRemoveExistingDevice() {
        room.addDevice(mockDevice1);
        room.addDevice(mockDevice2);

        room.removeDevice(mockDevice1);

        assertEquals(1, room.getDevices().size(), "Nach dem Entfernen sollte nur noch ein Gerät übrig sein.");
        assertFalse(room.getDevices().contains(mockDevice1), "Das entfernte Gerät sollte nicht mehr in der Liste sein.");
        assertTrue(room.getDevices().contains(mockDevice2), "Das andere Gerät sollte weiterhin existieren.");
    }

    @Test
    void removeDeviceShouldDoNothingIfDeviceNotPresent() {
        room.addDevice(mockDevice1);

        // Versuch, ein Gerät zu entfernen, das gar nicht in der Liste ist
        room.removeDevice(mockDevice2);

        assertEquals(1, room.getDevices().size(), "Die Größe der Liste sollte sich nicht ändern.");
        assertTrue(room.getDevices().contains(mockDevice1), "Das vorhandene Gerät sollte unangetastet bleiben.");
    }
}