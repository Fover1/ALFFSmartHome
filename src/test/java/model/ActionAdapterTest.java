package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionAdapterTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        // Gson mit unserem Adapter konfigurieren
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Action.class, new ActionAdapter());
        gson = builder.create();
    }

    @Test
    void testSerializeAndDeserialize() {
        TestAction originalAction = new TestAction("test-daten");

        // 1. Serialisieren (Java -> JSON)
        String json = gson.toJson(originalAction, Action.class);

        assertTrue(json.contains("className"));
        assertTrue(json.contains(TestAction.class.getName()));
        assertTrue(json.contains("data"));
        assertTrue(json.contains("test-daten"));

        // 2. Deserialisieren (JSON -> Java)
        Action deserializedAction = gson.fromJson(json, Action.class);

        assertInstanceOf(TestAction.class, deserializedAction);
        assertEquals("test-daten", ((TestAction) deserializedAction).value);
    }

    @Test
    void testDeserializeThrowsExceptionOnUnknownClass() {
        // Ein JSON-String mit einer Klasse, die es nicht gibt
        String invalidJson = "{\"className\":\"com.example.GibtsNichtAction\",\"data\":{}}";

        JsonParseException exception = assertThrows(
                JsonParseException.class,
                () -> gson.fromJson(invalidJson, Action.class)
        );

        assertTrue(exception.getMessage().contains("Unbekannte Aktion im JSON"));
    }

    // Eine kleine Dummy-Action für den Test
    public static class TestAction implements Action {
        public String value;

        public TestAction(String value) {
            this.value = value;
        }

        @Override
        public void execute() {
        }

        @Override
        public String getDescription() {
            return value;
        }
    }
}