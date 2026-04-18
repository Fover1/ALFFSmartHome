package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartDeviceAdapterTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        // Gson mit unserem Adapter konfigurieren
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SmartDevice.class, new SmartDeviceAdapter());
        gson = builder.create();
    }

    @Test
    void testSerializeAndDeserialize() {
        TestSmartDevice originalDevice = new TestSmartDevice("gerät-123");

        // 1. Serialisieren (Java -> JSON)
        String json = gson.toJson(originalDevice, SmartDevice.class);

        assertTrue(json.contains("className"));
        assertTrue(json.contains(TestSmartDevice.class.getName()));
        assertTrue(json.contains("data"));
        assertTrue(json.contains("gerät-123"));

        // 2. Deserialisieren (JSON -> Java)
        SmartDevice deserializedDevice = gson.fromJson(json, SmartDevice.class);

        assertInstanceOf(TestSmartDevice.class, deserializedDevice);
        assertEquals("gerät-123", ((TestSmartDevice) deserializedDevice).id);
    }

    @Test
    void testDeserializeThrowsExceptionOnUnknownClass() {
        // Ein JSON-String mit einer Klasse, die es nicht gibt
        String invalidJson = "{\"className\":\"com.example.GibtsNichtDevice\",\"data\":{}}";

        JsonParseException exception = assertThrows(
                JsonParseException.class,
                () -> gson.fromJson(invalidJson, SmartDevice.class)
        );

        assertTrue(exception.getMessage().contains("Unbekannter Gerätetyp im JSON"));
    }

    // Ein kleines Dummy-Device für den Test
    public static class TestSmartDevice implements SmartDevice {
        public String id;

        public TestSmartDevice(String id) {
            this.id = id;
        }

        @Override
        public void executeFunction(String functionName, Object parameter) {
        }

        @Override
        public List<String> getAvailableFunctions() {
            return null;
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public String getName() {
            return id;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public Room getRoom() {
            return null;
        }

        @Override
        public void setRoom(Room room) {

        }

        @Override
        public String getDeviceType() {
            return "";
        }

        @Override
        public String getCurrentState() {
            return "";
        }
    }
}