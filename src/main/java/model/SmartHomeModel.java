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

    public void addDevice(Room room, SmartDevice device) {
        room.addDevice(device);
    }

    public void removeDevice(SmartDevice device, Room oldRoom) {
        oldRoom.removeDevice(device);
    }

    public void changeDeviceRoom(SmartDevice device, Room oldRoom, Room newRoom) {
        removeDevice(device, oldRoom);

        // 2. Gerät dem neuen Raum hinzufügen
        newRoom.addDevice(device);
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    public void removeScenario(Scenario scenario) {
        scenarios.remove(scenario);
    }

    public List<SmartDevice> getAllDevices() {
        List<SmartDevice> allDevices = new ArrayList<>();
        for (Room room : rooms) {
            allDevices.addAll(room.getSmartDevices());
        }
        return allDevices;
    }

    public void changeDeviceName(SmartDevice device, String name) {
        device.setName(name);
    }

}