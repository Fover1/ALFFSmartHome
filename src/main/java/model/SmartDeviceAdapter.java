package model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SmartDeviceAdapter implements JsonSerializer<SmartDevice>, JsonDeserializer<SmartDevice> {

    @Override
    public JsonElement serialize(SmartDevice src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("className", src.getClass().getName());
        result.add("data", context.serialize(src, src.getClass()));
        return result;
    }

    @Override
    public SmartDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get("className").getAsString();
        JsonElement data = jsonObject.get("data");

        try {
            Class<?> clazz = Class.forName(className);
            return context.deserialize(data, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unbekannter Gerätetyp im JSON: " + className, e);
        }
    }
}