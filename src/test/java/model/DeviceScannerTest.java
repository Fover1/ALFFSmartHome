package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceScannerTest {

    private final File pluginFolder = new File("devices");
    private boolean createdPluginFolder = false;
    private File dummyExternalClass;
    private File dummyInnerClass;
    private File dummyTextFile;

    @BeforeEach
    void setUp() throws IOException {
        // Bereite den externen Ordner "devices" im Projektstamm vor, falls er nicht existiert
        if (!pluginFolder.exists()) {
            createdPluginFolder = pluginFolder.mkdir();
        }

        // Dummy-Dateien im externen Ordner erstellen, um den Scanner zu testen
        dummyExternalClass = new File(pluginFolder, "ExternalTestDevice.class");
        dummyExternalClass.createNewFile();

        // Eine innere Klasse (mit $), die vom Scanner ignoriert werden soll
        dummyInnerClass = new File(pluginFolder, "ExternalTestDevice$Inner.class");
        dummyInnerClass.createNewFile();

        // Eine Textdatei erstellen, die ebenfalls ignoriert werden soll
        dummyTextFile = new File(pluginFolder, "NotAClass.txt");
        dummyTextFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        // Aufräumen, damit der Workspace sauber bleibt und keine Mülldateien hinterlassen werden
        if (dummyExternalClass != null && dummyExternalClass.exists()) {
            dummyExternalClass.delete();
        }
        if (dummyInnerClass != null && dummyInnerClass.exists()) {
            dummyInnerClass.delete();
        }
        if (dummyTextFile != null && dummyTextFile.exists()) {
            dummyTextFile.delete();
        }
        // Den Ordner nur löschen, wenn wir ihn im Setup() selbst erstellt haben
        if (createdPluginFolder && pluginFolder.exists()) {
            pluginFolder.delete();
        }
    }

    @Test
    void testGetAllDeviceTypes_InternalAndExternalClasses() {
        // Act: Wir nutzen das "devices"-Package, das wir in den vorherigen Tests (DeviceFactory) angelegt haben.
        // Der ClassLoader sollte dort unsere kompilierten Test-Klassen finden.
        List<String> deviceTypes = DeviceScanner.getAllDeviceTypes("devices");

        // Assert
        assertNotNull(deviceTypes);

        // Prüfen, ob die internen Test-Klassen aus dem 'devices' Package gefunden wurden
        assertTrue(deviceTypes.contains("TestDevice"), "Sollte die interne TestDevice-Klasse finden");
        assertTrue(deviceTypes.contains("InvalidDevice"), "Sollte die interne InvalidDevice-Klasse finden");

        // Prüfen, ob die externe Klasse aus dem temporären Ordner "devices" gefunden wurde
        assertTrue(deviceTypes.contains("ExternalTestDevice"), "Sollte die externe ExternalTestDevice-Klasse finden");

        // Prüfen, ob innere Klassen (mit $) und fremde Dateitypen korrekt ignoriert wurden
        assertFalse(deviceTypes.contains("ExternalTestDevice$Inner"), "Sollte innere Klassen ignorieren");
        assertFalse(deviceTypes.contains("NotAClass"), "Sollte Nicht-.class-Dateien ignorieren");
    }

    @Test
    void testGetAllDeviceTypes_InvalidPackageName() {
        // Act: Wir übergeben einen Package-Namen, den es im System definitiv nicht gibt.
        // Der interne Scanner sollte abbrechen, ohne abzustürzen, und zum externen Scanner übergehen.
        List<String> deviceTypes = DeviceScanner.getAllDeviceTypes("ein.package.das.nicht.existiert");

        // Assert
        assertNotNull(deviceTypes);
        assertFalse(deviceTypes.contains("TestDevice"), "Darf interne Klassen nicht finden, da das Package falsch ist");

        // Die externen Klassen müssen trotzdem gefunden werden, weil der zweite Block unabhängig läuft
        assertTrue(deviceTypes.contains("ExternalTestDevice"), "Sollte weiterhin die externen Klassen finden");
    }
}