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
        if (!pluginFolder.exists()) {
            createdPluginFolder = pluginFolder.mkdir();
        }

        dummyExternalClass = new File(pluginFolder, "ExternalTestDevice.class");
        dummyExternalClass.createNewFile();
        dummyInnerClass = new File(pluginFolder, "ExternalTestDevice$Inner.class");
        dummyInnerClass.createNewFile();
        dummyTextFile = new File(pluginFolder, "NotAClass.txt");
        dummyTextFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        if (dummyExternalClass != null && dummyExternalClass.exists()) {
            dummyExternalClass.delete();
        }
        if (dummyInnerClass != null && dummyInnerClass.exists()) {
            dummyInnerClass.delete();
        }
        if (dummyTextFile != null && dummyTextFile.exists()) {
            dummyTextFile.delete();
        }
        if (createdPluginFolder && pluginFolder.exists()) {
            pluginFolder.delete();
        }
    }

    @Test
    void testGetAllDeviceTypes_InternalAndExternalClasses() {
        List<String> deviceTypes = DeviceScanner.getAllDeviceTypes("devices");

        assertNotNull(deviceTypes);
        assertTrue(deviceTypes.contains("TestDevice"), "Sollte die interne TestDevice-Klasse finden");
        assertTrue(deviceTypes.contains("InvalidDevice"), "Sollte die interne InvalidDevice-Klasse finden");
        assertTrue(deviceTypes.contains("ExternalTestDevice"), "Sollte die externe ExternalTestDevice-Klasse finden");
        assertFalse(deviceTypes.contains("ExternalTestDevice$Inner"), "Sollte innere Klassen ignorieren");
        assertFalse(deviceTypes.contains("NotAClass"), "Sollte Nicht-.class-Dateien ignorieren");
    }

    @Test
    void testGetAllDeviceTypes_InvalidPackageName() {
        List<String> deviceTypes = DeviceScanner.getAllDeviceTypes("ein.package.das.nicht.existiert");

        assertNotNull(deviceTypes);
        assertFalse(deviceTypes.contains("TestDevice"), "Darf interne Klassen nicht finden, da das Package falsch ist");
        assertTrue(deviceTypes.contains("ExternalTestDevice"), "Sollte weiterhin die externen Klassen finden");
    }
}