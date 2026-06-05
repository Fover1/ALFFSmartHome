package model;

import interfaces.DeviceFunction;
import interfaces.DeviceObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractDeviceTest {

    private DummyDevice device;
    private final UUID testId = UUID.randomUUID();
    private final String testName = "Test Gerät";

    @Mock
    private DeviceObserver mockObserver;

    @Mock
    private DeviceFunction mockFunction;

    class DummyDevice extends AbstractDevice {

        public DummyDevice(UUID id, String name) {
            super(id, name);
        }

        @Override
        protected void initializeFunctions() {
            this.functions.put("Testfunktion", mockFunction);
        }

        @Override
        public String getDeviceType() {
            return "DummyType";
        }

        @Override
        public String getCurrentState() {
            return "DummyState";
        }
    }

    @BeforeEach
    void setUp() {
        device = new DummyDevice(testId, testName);
    }

    @Test
    void testConstructorAndLombokGetters() {
        assertEquals(testId, device.getId());
        assertEquals(testName, device.getName());
        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
    }

    @Test
    void testLombokSetters() {
        device.setName("Test");
        assertEquals("Test", device.getName());

        List<DeviceObserver> newObservers = new ArrayList<>();
        device.setObservers(newObservers);
        assertEquals(newObservers, device.getObservers());
    }

    @Test
    void testRestoreAfterLoadNullLists() {
        device.setObservers(null);
        device.setFunctions(null);
        device.restoreAfterLoad();

        assertNotNull(device.getObservers());
        assertNotNull(device.getFunctions());
        assertTrue(device.getFunctions().containsKey("Testfunktion"));
    }

    @Test
    void testRestoreAfterLoadEmptyFunctions() {
        device.getFunctions().clear();
        device.restoreAfterLoad();

        assertTrue(device.getFunctions().containsKey("Testfunktion"));
    }

    @Test
    void testNotifyObservers() {
        device.addObserver(mockObserver);
        device.notifyObservers();

        verify(mockObserver).onStateChanged(device);
    }

    @Test
    void testGetAvailableFunctions() {
        List<String> functions = device.getAvailableFunctions();
        assertEquals(1, functions.size());
        assertTrue(functions.contains("Testfunktion"));
    }

    @Test
    void testGetFunction() {
        DeviceFunction function = device.getFunction("Testfunktion");
        assertEquals(mockFunction, function);
        assertNull(device.getFunction("NichtVorhanden"));
    }

    @Test
    void testExecuteFunctionSuccess() {
        Object param = 42;
        device.addObserver(mockObserver);
        device.executeFunction("Testfunktion", param);

        verify(mockFunction).execute(param);
        verify(mockObserver).onStateChanged(device);
    }

    @Test
    void testExecuteFunctionNotFoundThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            device.executeFunction("UnbekannteFunktion", null);
        });

        assertTrue(exception.getMessage().contains("UnbekannteFunktion"));
    }

    @Test
    void testAbstractMethods() {
        assertEquals("DummyType", device.getDeviceType());
        assertEquals("DummyState", device.getCurrentState());
    }
}