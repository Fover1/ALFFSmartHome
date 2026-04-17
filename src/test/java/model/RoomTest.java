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
    private AbstractDevice mockAbstractDevice1;
    private AbstractDevice mockAbstractDevice2;

    @BeforeEach
    void setUp() {
        // Wird vor jedem Test ausgeführt, um einen sauberen Zustand zu haben
        room = new Room("Wohnzimmer");
        mockAbstractDevice1 = mock(AbstractDevice.class);
        mockAbstractDevice2 = mock(AbstractDevice.class);
    }

    @Test
    void constructorShouldSetNameAndInitializeEmptyList() {
        assertEquals("Wohnzimmer", room.getName(), "Der Name des Raums sollte korrekt gesetzt werden.");
        assertNotNull(room.getAbstractDevices(), "Die Geräteliste sollte nicht null sein.");
        assertTrue(room.getAbstractDevices().isEmpty(), "Die Geräteliste sollte anfangs leer sein.");
    }

    @Test
    void setNameShouldUpdateName() {
        room.setName("Schlafzimmer");
        assertEquals("Schlafzimmer", room.getName(), "Der Setter sollte den Namen des Raums aktualisieren.");
    }

    @Test
    void setDevicesShouldUpdateDeviceList() {
        List<AbstractDevice> newList = new ArrayList<>();
        newList.add(mockAbstractDevice1);

        room.setAbstractDevices(newList);

        assertEquals(1, room.getAbstractDevices().size());
        assertTrue(room.getAbstractDevices().contains(mockAbstractDevice1));
    }

    @Test
    void addDeviceShouldAddDeviceIfNotPresent() {
        room.addDevice(mockAbstractDevice1);

        assertEquals(1, room.getAbstractDevices().size(), "Die Liste sollte genau ein Gerät enthalten.");
        assertTrue(room.getAbstractDevices().contains(mockAbstractDevice1), "Das hinzugefügte Gerät sollte in der Liste sein.");
    }

    @Test
    void addDeviceShouldNotAddDuplicateDevice() {
        room.addDevice(mockAbstractDevice1);
        room.addDevice(mockAbstractDevice1); // Versuch, dasselbe Gerät noch einmal hinzuzufügen

        assertEquals(1, room.getAbstractDevices().size(), "Duplikate sollten nicht hinzugefügt werden.");
    }

    @Test
    void removeDeviceShouldRemoveExistingDevice() {
        room.addDevice(mockAbstractDevice1);
        room.addDevice(mockAbstractDevice2);

        room.removeDevice(mockAbstractDevice1);

        assertEquals(1, room.getAbstractDevices().size(), "Nach dem Entfernen sollte nur noch ein Gerät übrig sein.");
        assertFalse(room.getAbstractDevices().contains(mockAbstractDevice1), "Das entfernte Gerät sollte nicht mehr in der Liste sein.");
        assertTrue(room.getAbstractDevices().contains(mockAbstractDevice2), "Das andere Gerät sollte weiterhin existieren.");
    }

    @Test
    void removeDeviceShouldDoNothingIfDeviceNotPresent() {
        room.addDevice(mockAbstractDevice1);

        // Versuch, ein Gerät zu entfernen, das gar nicht in der Liste ist
        room.removeDevice(mockAbstractDevice2);

        assertEquals(1, room.getAbstractDevices().size(), "Die Größe der Liste sollte sich nicht ändern.");
        assertTrue(room.getAbstractDevices().contains(mockAbstractDevice1), "Das vorhandene Gerät sollte unangetastet bleiben.");
    }
}