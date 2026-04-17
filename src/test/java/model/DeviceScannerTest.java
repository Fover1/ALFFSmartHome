package model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceScannerTest {

    @Test
    void testGetAllDeviceTypes() {
        String testPackage = "devices";

        List<String> result = DeviceScanner.getAllDeviceTypes(testPackage);

        assertNotNull(result);
    }

    @Test
    void testInvalidPackage() {
        List<String> result = DeviceScanner.getAllDeviceTypes("dieses.package.existiert.nicht");

        assertTrue(result.isEmpty());
    }
}