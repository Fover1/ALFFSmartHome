package model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static lang.ErrorMessages.CLASS_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceFactoryTest {

    @Test
    void testCreateDeviceSuccess() {
        Room roomMock = Mockito.mock(Room.class);
        String id = "1";
        String name = "Wohnzimmerlampe";

        AbstractDevice device = DeviceFactory.createDevice("Light", id, name, roomMock);

        assertNotNull(device);
        assertEquals(id, device.getId());
        assertEquals(name, device.getName());
        assertEquals(roomMock, device.getRoom());
        assertEquals("devices.Light", device.getClass().getName());
    }

    @Test
    void testCreateDeviceNotFound() {
        Room mockRoom = Mockito.mock(Room.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            DeviceFactory.createDevice("NonExistentDevice", "2", "Name", mockRoom);
        });

        assertTrue(ex.getMessage().contains(CLASS_NOT_FOUND));
    }

    @Test
    void testCreateDeviceError() {
        Room mockRoom = Mockito.mock(Room.class);

        assertThrows(RuntimeException.class, () -> {
            DeviceFactory.createDevice("deviceScannerTest", "3", "Name", mockRoom);
        });
    }
}