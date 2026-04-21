package controller;

import model.AbstractDevice;
import model.PersistenceManager;
import model.Room;
import model.SmartHomeModel;

import java.util.List;

public class SmartHomeAppController {

    private final SmartHomeModel smartHomeModel;

    public SmartHomeAppController() {
        this.smartHomeModel = new SmartHomeModel();
        loadInitialData();
    }

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

    public void removeRoom(Room room) {
        smartHomeModel.removeRoom(room);
        save();
    }

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

    // Hilfsmethode, damit das Frontend leicht an die Geräte kommt
    public List<AbstractDevice> getAllDevices() {
        return smartHomeModel.getAllDevices();
    }
}