package model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import static lang.ErrorMessages.UNKNOWN_ACTION_JSON;

//Da GSON beim Serialisieren/Deserialisieren der JSON nicht weiß, was er für eine Art Action vor sich hat (Action ist ein Interface das von anderen konkreten Klassen implementiert wird), braucht es diesen Adapter
//Also weiß nicht, ob es jetzt eine DeviceAction oder ein Szenario war //TODO ????
public class ActionAdapter implements JsonSerializer<Action>, JsonDeserializer<Action> {

    //JsonElement: Erzeugt Baum aus Action Informationen
    @Override
    public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        //holt den Klassennamen (also z.B. deviceAction / Scenario), um später zu erkennen, welche konkrete Klasse hier mit dem Interface implementiert wurde
        result.addProperty("className", src.getClass().getName());
        //context.serialize(src, src.getClass()): Erstellt ein JsonElement, hier wird noch nichts gespeichert
        result.add("data", context.serialize(src, src.getClass()));
        return result;
    }

    //wandelt Json Elemente wieder in Java Objekte um
    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //nimmt sich ein Objekt aus der Action Sektion
        JsonObject jsonObject = json.getAsJsonObject();
        //holt sich den ClassName (was ist das für eine Art Action) //TODO ???
        String className = jsonObject.get("className").getAsString();
        //holt sich die Daten der konkreten Action
        JsonElement data = jsonObject.get("data");

        try {
            //wenn es den gefundenen Klassennamen gibt
            Class<?> clazz = Class.forName(className);
            //kann das Objekt automatisch deserialisiert werden
            return context.deserialize(data, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(UNKNOWN_ACTION_JSON + className, e);
        }
    }
}