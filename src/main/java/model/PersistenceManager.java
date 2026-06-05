package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import interfaces.Action;
import interfaces.SmartDevice;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import static lang.ErrorMessages.DEVICE_FOR_SCENARIO_NOT_FOUND;
import static lang.ErrorMessages.ERROR_LOADING_JSON;

@Setter
public class PersistenceManager {

    private static String FILE_NAME = "smarthome_config.json";

    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                //registerTypeAdapter: wird benoetigt, damit Builder genau weiß, wie er mit den verschiedenen Interfaces und abstrakten Klassen umgehen muss
                //brauchen das fuer diese Klassen, da nicht alle Infos in der Json stehen (anders als bei z.B. Raum)
                .registerTypeAdapter(SmartDevice.class, new SmartDeviceAdapter())
                .registerTypeAdapter(Action.class, new ActionAdapter())
                .create();
    }

    public static void save(List<Room> rooms, List<Scenario> scenarios) {
        //oeffnet Verbindung zur Json (Festplatte)
        //Datei wird am Ende automatisch geschlossen
        try (Writer writer = new FileWriter(FILE_NAME)) {
            SmartHomeData data = new SmartHomeData(rooms, scenarios);
            createGson().toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public static SmartHomeData load(String FileName) {
        File file = new File(FileName);
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            //liest die Datei ein und erstellt die Objekte
            SmartHomeData data = createGson().fromJson(reader, SmartHomeData.class);

            //Problem: transient Felder sind null
            if (data != null && data.rooms != null) {
                for (Room room : data.rooms) {
                    for (SmartDevice device : room.getSmartDevices()) {
                        //wird fuer jedes Geraet aufgerufen das es gibt, um die transient Felder neu zu initialisieren
                        device.restoreAfterLoad();

                    }
                }
            }

            if (data != null) {
                linkScenariosToRealDevices(data);
            }
            return data;

        } catch (Exception e) {
            System.err.println(ERROR_LOADING_JSON + e.getMessage());
            return null;
        }
    }

    //DTO (Data Transfer Objekt)
    private static SmartDevice findRealDeviceById(List<Room> rooms, String targetId) {
        for (Room room : rooms) {
            for (SmartDevice device : room.getSmartDevices()) {
                if (String.valueOf(device.getId()).equals(targetId)) {
                    return device;
                }
            }

        }
        return null;
    }

    public static void linkScenariosToRealDevices(SmartHomeData smartHomeData) {
        if (smartHomeData.scenarios == null) {
            return;
        }
        for (Scenario scenario : smartHomeData.scenarios) {
            List<Action> actions = scenario.getActions();
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if (action instanceof DeviceAction) {
                    String cloneId = String.valueOf(((DeviceAction) action).getTargetDevice().getId());
                    SmartDevice realDevice = findRealDeviceById(smartHomeData.rooms, cloneId);

                    if (realDevice != null) {
                        DeviceAction newDeviceAction = new DeviceAction(
                                realDevice,
                                ((DeviceAction) action).getFunctionName(),
                                ((DeviceAction) action).getParameter()
                        );
                        actions.set(i, newDeviceAction);
                    } else {
                        System.out.println(DEVICE_FOR_SCENARIO_NOT_FOUND);
                    }
                }
            }
        }
    }

    public static void setFileName(String fileName) {
        FILE_NAME = fileName;
    }

    public static class SmartHomeData {
        public List<Room> rooms;
        public List<Scenario> scenarios;

        //Json speichert eigentlich nur eine Sache in einer Datei. Um die beiden verschiedenen Listen zusammen in eine Datei zu bekommen, brauchen wir das hier:
        public SmartHomeData(List<Room> rooms, List<Scenario> scenarios) {
            this.rooms = rooms;
            this.scenarios = scenarios;
        }
    }
}