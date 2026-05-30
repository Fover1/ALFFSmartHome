package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

@Setter
public class PersistenceManager {

    private static String FILE_NAME = "smarthome_config.json";

    private static Gson createGson() {
        return new GsonBuilder()
                //schöndruck
                .setPrettyPrinting()
                //hier müssen wir nochmal schauen, das ist noch nicht so schön
                //braucht aktuell die beiden dinger, weil
                //wird benötigt, damit er genau weiß, wie er mit den verschiedenen Interfaces und abtrakten klassen umgehen muss
                //brauchen das für diese Klassen, da nicht alle Infos in der Json stehen (anders als bei z.B. Raum)
//                .registerTypeAdapter(AbstractDevice.class, new SmartDeviceAdapter())
                .registerTypeAdapter(SmartDevice.class, new SmartDeviceAdapter())
//                .registerTypeHierarchyAdapter(SmartDevice.class, new SmartDeviceAdapter())
                .registerTypeAdapter(Action.class, new ActionAdapter())
                .create();
    }

    public static void save(List<Room> rooms, List<Scenario> scenarios) {
        //öffnet Verbindung zur Json (Festplatte)
        //Datei wird am ende automatisch geschlossen
        try (Writer writer = new FileWriter(FILE_NAME)) {
            SmartHomeData data = new SmartHomeData(rooms, scenarios);
            createGson().toJson(data, writer);
            System.out.println("Konfiguration erfolgreich als JSON gespeichert.");
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

            //problem: transient felder sind null
            //es wird bei jedem Gerät neu gemacht
            if (data != null && data.rooms != null) {
                for (Room room : data.rooms) {
//                    if (room.getSmartDevices() == null) {
//                        room.setSmartDevices(new java.util.ArrayList<>());
//                    }
                    for (SmartDevice device : room.getSmartDevices()) {
                        //wird für jedes Gerät aufgerufen, das es gibt um die transient felder neu zu initialisieren
                        device.restoreAfterLoad();

                    }
                }
            }

            if (data != null) {
                linkScenariosToRealDevices(data);
            }
            System.out.println("Konfiguration erfolgreich geladen.");
            return data;

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der JSON-Datei: " + e.getMessage());
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
                        System.out.println("Achtung: Gerät für Szenario nicht gefunden!");
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

        //Json speichert eigetnlich nur eine Sache in einer Datei. Um die beiden verschiedenen Listen zusammen in eine Datei zu bekommen brauchen wir das hier
        public SmartHomeData(List<Room> rooms, List<Scenario> scenarios) {
            this.rooms = rooms;
            this.scenarios = scenarios;
        }
    }
}