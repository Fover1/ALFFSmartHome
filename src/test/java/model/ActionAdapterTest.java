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
        DummyAction dummyAction = new DummyAction();
        JsonSerializationContext mockContext = mock(JsonSerializationContext.class);

        //Simulation, was der Context zurueckgeben wuerde, wenn er die inneren Daten serialisiert
        JsonObject mockSerializedData = new JsonObject();
        mockSerializedData.addProperty("dummyField", "dummyValue");

        when(mockContext.serialize(dummyAction, DummyAction.class)).thenReturn(mockSerializedData);

        JsonElement result = adapter.serialize(dummyAction, Action.class, mockContext);

        assertTrue(result.isJsonObject(), "Das Ergebnis muss ein JsonObject sein");
        JsonObject resultObject = result.getAsJsonObject();

        assertEquals(DummyAction.class.getName(), resultObject.get("className").getAsString());
        assertEquals(mockSerializedData, resultObject.get("data"));
    }

    @Test
    void testDeserializeSuccess()  {
        JsonObject jsonToDeserialize = new JsonObject();
        jsonToDeserialize.addProperty("className", DummyAction.class.getName());

        JsonObject dataObject = new JsonObject();
        jsonToDeserialize.add("data", dataObject);

        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);
        DummyAction expectedAction = new DummyAction();

        when(mockContext.deserialize(dataObject, DummyAction.class)).thenReturn(expectedAction);
        Action result = adapter.deserialize(jsonToDeserialize, Action.class, mockContext);

        assertEquals(expectedAction, result, "Die deserialisierte Aktion sollte der erwarteten Aktion entsprechen");
    }

    @Test
    void testDeserializeThrowsJsonParseExceptionWhenClassIsUnknown() {
        JsonObject jsonToDeserialize = new JsonObject();
        jsonToDeserialize.addProperty("className", "com.meinprojekt.GibtEsNichtAction");
        jsonToDeserialize.add("data", new JsonObject());
        JsonDeserializationContext mockContext = mock(JsonDeserializationContext.class);

        JsonParseException exception = assertThrows(JsonParseException.class, () -> {
            adapter.deserialize(jsonToDeserialize, Action.class, mockContext);
        });

        assertTrue(exception.getMessage().contains("com.meinprojekt.GibtEsNichtAction"));
    }

    static class DummyAction implements Action {
        @Override
        public void execute() {}

        @Override
        public void undo() {}

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