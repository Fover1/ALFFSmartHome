package model; // oder package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class PersistenceManager {

    private static final String FILE_NAME = "smarthome_config.json";

    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(AbstractDevice.class, new SmartDeviceAdapter())
                .registerTypeAdapter(SmartDevice.class, new SmartDeviceAdapter())
                .registerTypeAdapter(Action.class, new ActionAdapter())
                .create();
    }

    public static void save(List<Room> rooms, List<Scenario> scenarios) {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            SmartHomeData data = new SmartHomeData(rooms, scenarios);
            createGson().toJson(data, writer);
            System.out.println("Konfiguration erfolgreich als JSON gespeichert.");
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public static SmartHomeData load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            SmartHomeData data = createGson().fromJson(reader, SmartHomeData.class);

            if (data != null && data.rooms != null) {
                for (Room room : data.rooms) {
                    for (SmartDevice device : room.getAbstractDevices()) {
                        if (device instanceof AbstractDevice) {
                            ((AbstractDevice) device).restoreAfterLoad();
                        }
                    }
                }
            }
            System.out.println("Konfiguration erfolgreich geladen.");
            return data;

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der JSON-Datei: " + e.getMessage());
            return null;
        }
    }

    public static class SmartHomeData {
        public List<Room> rooms;
        public List<Scenario> scenarios;

        public SmartHomeData(List<Room> rooms, List<Scenario> scenarios) {
            this.rooms = rooms;
            this.scenarios = scenarios;
        }
    }
}