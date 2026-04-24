package controller;

import model.AbstractDevice;
import model.PersistenceManager;
import model.Room;
import model.SmartHomeModel;

import java.util.List;

public class SmartHomeAppController {

    //verbindung zwischen Model (SmartHomeModel) und fester Datenspeicherung (PersistenceManager)
    //Methoden werden teilweise auch von der GUI abgerufen
    private final SmartHomeModel smartHomeModel;

    public SmartHomeAppController() {
        this.smartHomeModel = new SmartHomeModel();
        loadInitialData();
    }

    //lädt die Daten aus der JSON in den nur für die Session verfügbaren smartHomeModelSpeicher
    private void loadInitialData() {
        PersistenceManager.SmartHomeData data = PersistenceManager.load();
        if (data != null) {
            if (data.rooms != null) smartHomeModel.setRooms(data.rooms);
            if (data.scenarios != null) smartHomeModel.setScenarios(data.scenarios);
        }
    }

    public void save() {
        PersistenceManager.save(smartHomeModel.getRooms(), smartHomeModel.getScenarios());
    }

    public void addRoom(String name) {
        smartHomeModel.addRoom(new Room(name));
        save();
    }

    public void deleteRoom(Room room) {
        smartHomeModel.removeRoom(room);
        save();
    }

    public void changeRoomName(Room room, String name) {
        smartHomeModel.changeRoomName(room, name);
    }

    public void changeDeviceName(AbstractDevice device, String name) {
        smartHomeModel.changeDeviceName(device, name);
    }

    public void deleteDevice(AbstractDevice device) {
        smartHomeModel.removeDevice(device);
    }

    public void changeDeviceRoom(AbstractDevice device, Room room) {
        smartHomeModel.changeDeviceRoom(device, room);
    }

    //Geräte ID´s werden automatisch generiert
//    ID´s: erste 3 Buchstaben des Klassennamens und dann ein counter hochzählend (bis auf 4 stellen mit null vorran aufgefüllt)

    /// todo: id vllt gegen uuid tauschen?
    // Ist ein String, damit man an der ID erkennen konnte, welche Art von Gerät es ist
    public String generateDeviceId(String deviceType) {
        String shortName = deviceType.length() >= 3 ? deviceType.substring(0, 3) : deviceType;
        String prefix = shortName.toUpperCase() + "-";

        int maxNumber = 0;
        for (AbstractDevice device : smartHomeModel.getAllDevices()) {
            if (device.getId().startsWith(prefix)) {
                try {
                    String numberPart = device.getId().substring(prefix.length());
                    maxNumber = Integer.parseInt(numberPart);
                } catch (NumberFormatException e) {
                    // TODO: Fehlerbehandlung (wie in deinem originalen Code)
                }
            }
        }
        return prefix + String.format("%04d", maxNumber + 1);
    }

    public List<AbstractDevice> getAllDevices() {
        return smartHomeModel.getAllDevices();
    }

    public List<Room> getAllRooms() {
        return smartHomeModel.getRooms();
    }
}