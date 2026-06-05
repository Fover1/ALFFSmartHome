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
    void testCreateDevice_Success() {
        // Arrange
        UUID expectedId = UUID.randomUUID();
        String expectedName = "Mein Test Gerät";

        // Act
        // Ruft unser Dummy-Gerät "TestDevice" aus dem devices-Package auf
        SmartDevice device = DeviceFactory.createDevice("TestDevice", expectedId, expectedName);

        // Assert
        assertNotNull(device, "Das erstellte Gerät darf nicht null sein");
        assertEquals(expectedId, device.getId());
        assertEquals(expectedName, device.getName());
    }

    @Test
    void testCreateDevice_ClassNotFoundThrowsIllegalArgumentException() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            DeviceFactory.createDevice("EinGeraetDasEsNichtGibt", id, "Name");
        });

        // Prüfen, ob der fehlerhafte Klassenname in der Exception steht
        assertTrue(exception.getMessage().contains("EinGeraetDasEsNichtGibt"));
    }

    @Test
    void testCreateDevice_MissingConstructorThrowsRuntimeException() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act & Assert
        // InvalidDevice existiert zwar, hat aber nicht den (UUID, String) Konstruktor
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            DeviceFactory.createDevice("InvalidDevice", id, "Name");
        });

        assertTrue(exception.getMessage().contains("InvalidDevice"));
    }
}