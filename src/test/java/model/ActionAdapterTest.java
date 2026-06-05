package model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import interfaces.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionAdapterTest {

    private ActionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ActionAdapter();
    }

    @Test
    void testSerialize() {
        // Arrange
        DummyAction dummyAction = new DummyAction();
        JsonSerializationContext mockContext = mock(JsonSerializationContext.class);

        // Simuliere, was der Context zurückgeben würde, wenn er die inneren Daten serialisiert
        JsonObject mockSerializedData = new JsonObject();
        mockSerializedData.addProperty("dummyField", "dummyValue");

        when(mockContext.serialize(dummyAction, DummyAction.class)).thenReturn(mockSerializedData);

        // Act
        JsonElement result = adapter.serialize(dummyAction, Action.class, mockContext);

        // Assert
        assertTrue(result.isJsonObject(), "Das Ergebnis muss ein JsonObject sein");
        JsonObject resultObject = result.getAsJsonObject();

        // Prüfen, ob der Klassenname korrekt hinterlegt wurde
        assertEquals(DummyAction.class.getName(), resultObject.get("className").getAsString());

        // Prüfen, ob das Daten-Feld die vom Context generierten Daten enthält
        assertEquals(mockSerializedData, resultObject.get("data"));
    }

    @Test
    void testDeserialize_Success() throws ClassNotFoundException {
        // Arrange
        JsonObject jsonToDeserialize = new JsonObject();
        jsonToDeserialize.addProperty("className", DummyAction.class.getName());

        JsonObject dataObject = new JsonObject();
        jsonToDeserialize.add("data", dataObject);

        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);
        DummyAction expectedAction = new DummyAction();

        // Wenn der Context gebeten wird, "dataObject" als DummyAction zu deserialisieren, gib expectedAction zurück
        when(mockContext.deserialize(dataObject, DummyAction.class)).thenReturn(expectedAction);

        // Act
        Action result = adapter.deserialize(jsonToDeserialize, Action.class, mockContext);

        // Assert
        assertEquals(expectedAction, result, "Die deserialisierte Aktion sollte der erwarteten Aktion entsprechen");
    }

    @Test
    void testDeserialize_ThrowsJsonParseException_WhenClassIsUnknown() {
        // Arrange
        JsonObject jsonToDeserialize = new JsonObject();
        // Einen Klassennamen angeben, den es im System garantiert nicht gibt
        jsonToDeserialize.addProperty("className", "com.meinprojekt.GibtEsNichtAction");
        jsonToDeserialize.add("data", new JsonObject());

        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);

        // Act & Assert
        JsonParseException exception = assertThrows(JsonParseException.class, () -> {
            adapter.deserialize(jsonToDeserialize, Action.class, mockContext);
        });

        // Prüfen, ob der Klassenname in der Fehlermeldung auftaucht (hilft beim Debuggen)
        assertTrue(exception.getMessage().contains("com.meinprojekt.GibtEsNichtAction"));
    }

    //Eine simple Implementierung des Action-Interfaces für den Test, um Mockito-Proxy-Klassennamen bei src.getClass() zu vermeiden.
    static class DummyAction implements Action {
        @Override
        public void execute() {
        }

        @Override
        public void undo() {
        }

        @Override
        public String getDescription() {
            return "Dummy";
        }

        @Override
        public String getName() {
            return "Dummy";
        }
    }
}