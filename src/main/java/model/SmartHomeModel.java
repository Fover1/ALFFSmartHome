package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SmartHomeModel {
    //zentraler Speicher--> alles was im Haus verfügbar ist, kann hier aufgerufen werden
    private List<Room> rooms = new ArrayList<>();
    private List<Scenario> scenarios = new ArrayList<>();

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public void changeRoomName(Room room, String name) {
        room.setName(name);
    }

    public void addDevice(Room room, AbstractDevice device) {
        room.addDevice(device);
    }

    public void removeDevice(AbstractDevice device) {
        /// todo: das gerät muss auch noch aus der json gelöscht werden
        /// todo: generell, wie ist das mit dem speichern in der json bei den verschiedenen aktionen
        device.getRoom().removeDevice(device);
    }

    public void changeDeviceRoom(AbstractDevice device, Room room) {
        device.setRoom(room);
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    public void removeScenario(Scenario scenario) {
        scenarios.remove(scenario);
    }

    public List<AbstractDevice> getAllDevices() {
        List<AbstractDevice> allDevices = new ArrayList<>();
        for (Room room : rooms) {
            allDevices.addAll(room.getAbstractDevices());
        }
        return allDevices;
    }

    public void changeDeviceName(AbstractDevice device, String name) {
        device.setName(name);
    }
}