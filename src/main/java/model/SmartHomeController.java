package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SmartHomeController {
    private final List<Room> rooms;
    private final List<Scenario> scenarios;

    public SmartHomeController() {
        PersistenceManager.SmartHomeData data = PersistenceManager.load();

        if (data != null) {
            this.rooms = (data.rooms != null) ? data.rooms : new ArrayList<>();
            this.scenarios = (data.scenarios != null) ? data.scenarios : new ArrayList<>();
        } else {
            this.rooms = new ArrayList<>();
            this.scenarios = new ArrayList<>();
        }
    }

    public String generateDeviceId(String deviceType) {
        String shortName = deviceType.length() >= 3 ? deviceType.substring(0, 3) : deviceType;
        String prefix = shortName.toUpperCase() + "-";

        int maxNumber = 0;
        for (AbstractDevice device : getAllDevices()) {
            if (device.getId().startsWith(prefix)) {
                try {
                    String numberPart = device.getId().substring(prefix.length());
                    maxNumber = Integer.parseInt(numberPart);

                } catch (NumberFormatException e) {
                    ///  todo: hier muss noch ne fehlermeldung hin, wenn die ID vergabe nicht funktioniert hat. Muss der case noch weiter behandelt werden?
                }
            }
        }

        ///  todo: das ist hier noch nicht ganz so shcön, wenn es nicht funktioniert, bekommt das mopped die nummer 0
        return prefix + String.format("%04d", maxNumber + 1);
    }

    public void addRoom(String name) {
        rooms.add(new Room(name));
        save();
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        save();
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
        save();
    }

    public void removeScenario(Scenario scenario) {
        scenarios.remove(scenario);
        save();
    }

    public void save() {
        PersistenceManager.save(rooms, scenarios);
    }

    public List<AbstractDevice> getAllDevices() {
        List<AbstractDevice> allDevices = new ArrayList<>();
        for (Room room : rooms) {
            allDevices.addAll(room.getAbstractDevices());
        }
        return allDevices;
    }
}