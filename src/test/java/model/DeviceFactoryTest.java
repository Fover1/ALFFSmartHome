package model;

import interfaces.SmartDevice;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceFactoryTest {

    @Test
    void testCreateDeviceSuccess() {
        UUID expectedId = UUID.randomUUID();
        String expectedName = "Test Gerät";
        SmartDevice device = DeviceFactory.createDevice("TestDevice", expectedId, expectedName);

        assertNotNull(device, "Das erstellte Gerät darf nicht null sein");
        assertEquals(expectedId, device.getId());
        assertEquals(expectedName, device.getName());
    }

    @Test
    void testCreateDeviceClassNotFoundThrowsIllegalArgumentException() {
        UUID id = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            DeviceFactory.createDevice("Unbekanntes Gerät", id, "Name");
        });

        assertTrue(exception.getMessage().contains("Unbekanntes Gerät"));
    }

    @Test
    void testCreateDeviceMissingConstructorThrowsRuntimeException() {
        UUID id = UUID.randomUUID();

        //InvalidDevice existiert zwar, hat aber nicht den (UUID, String) Konstruktor
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            DeviceFactory.createDevice("InvalidDevice", id, "Name");
        });

        assertTrue(exception.getMessage().contains("InvalidDevice"));
    }
}