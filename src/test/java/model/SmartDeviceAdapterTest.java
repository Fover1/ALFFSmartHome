package model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmartDeviceAdapterTest {

    private SmartDeviceAdapter adapter;

    // Eine simple Implementierung für den Test, um echte Klassennamen bei der Serialisierung zu garantieren
    static class DummySmartDevice extends AbstractDevice {
        public DummySmartDevice() {
            super(UUID.randomUUID(), "Dummy Adapter Gerät");
        }

        @Override
        protected void initializeFunctions() {
            // Für diesen Test nicht relevant
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
        adapter = new SmartDeviceAdapter();
    }

    @Test
    void testSerialize() {
        // Arrange
        DummySmartDevice dummyDevice = new DummySmartDevice();
        JsonSerializationContext mockContext = mock(JsonSerializationContext.class);

        // Simuliere die serialisierten inneren Daten
        JsonObject mockSerializedData = new JsonObject();
        mockSerializedData.addProperty("id", dummyDevice.getId().toString());
        mockSerializedData.addProperty("name", dummyDevice.getName());

        when(mockContext.serialize(dummyDevice, DummySmartDevice.class)).thenReturn(mockSerializedData);

        // Act
        JsonElement result = adapter.serialize(dummyDevice, SmartDevice.class, mockContext);

        // Assert
        assertTrue(result.isJsonObject(), "Das Ergebnis muss ein JsonObject sein");
        JsonObject resultObject = result.getAsJsonObject();

        assertEquals(DummySmartDevice.class.getName(), resultObject.get("className").getAsString(),
                "Der Klassenname muss exakt übereinstimmen");
        assertEquals(mockSerializedData, resultObject.get("data"),
                "Die Daten müssen aus dem Context übernommen werden");
    }

    @Test
    void testDeserialize_Success() throws ClassNotFoundException {
        // Arrange
        JsonObject jsonToDeserialize = new JsonObject();
        jsonToDeserialize.addProperty("className", DummySmartDevice.class.getName());

        JsonObject dataObject = new JsonObject();
        jsonToDeserialize.add("data", dataObject);

        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);
        DummySmartDevice expectedDevice = new DummySmartDevice();

        when(mockContext.deserialize(dataObject, DummySmartDevice.class)).thenReturn(expectedDevice);

        // Act
        SmartDevice result = adapter.deserialize(jsonToDeserialize, SmartDevice.class, mockContext);

        // Assert
        assertEquals(expectedDevice, result, "Das deserialisierte Gerät muss dem erwarteten Mock-Objekt entsprechen");
    }

    @Test
    void testDeserialize_ThrowsJsonParseException_WhenClassIsUnknown() {
        // Arrange
        JsonObject jsonToDeserialize = new JsonObject();
        jsonToDeserialize.addProperty("className", "com.meinprojekt.GibtEsNichtDevice");
        jsonToDeserialize.add("data", new JsonObject());

        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);

        // Act & Assert
        JsonParseException exception = assertThrows(JsonParseException.class, () -> {
            adapter.deserialize(jsonToDeserialize, SmartDevice.class, mockContext);
        });

        assertTrue(exception.getMessage().contains("com.meinprojekt.GibtEsNichtDevice"),
                "Die Exception-Nachricht sollte den fehlerhaften Klassennamen enthalten");
    }
}