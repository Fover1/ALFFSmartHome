package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// todo: das ist ein generierter test und muss nochmal überarbeitet werden

class DeviceActionTest {

    @Test
    void testExecute() {
        // Mock erstellen
        SmartDevice mockDevice = mock(SmartDevice.class);

        // Aktion erstellen
        DeviceAction action = new DeviceAction(mockDevice, "turnOn", "high");

        // Methode ausführen
        action.execute();

        // Verifizieren, dass das Device korrekt aufgerufen wurde
        verify(mockDevice, times(1)).executeFunction("turnOn", "high");
    }

    @Test
    void testGetDescription() {
        // Mock erstellen und Verhalten definieren
        SmartDevice mockDevice = mock(SmartDevice.class);
        when(mockDevice.getName()).thenReturn("Wohnzimmerlampe");

        // Aktion erstellen
        DeviceAction action = new DeviceAction(mockDevice, "setBrightness", 50);

        // Testen, ob der String korrekt zusammengebaut wird
        assertEquals("Wohnzimmerlampe -> setBrightness 50", action.getDescription());
    }
}