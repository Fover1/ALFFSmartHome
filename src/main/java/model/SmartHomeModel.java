package model;

import interfaces.SmartDevice;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//zentraler Speicher: Alles was im Haus verfuegbar ist, kann hier aufgerufen werden
public class SmartHomeModel {
    private List<Room> rooms = new ArrayList<>();
    private List<Scenario> scenarios = new ArrayList<>();

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        if (room.getSmartDevices() != null) {
            for (SmartDevice device : room.getSmartDevices()) {
                cleanUpScenariosForDevice(device);
            }
        }
        rooms.remove(room);
    }

    private void cleanUpScenariosForDevice(SmartDevice device) {
        for (Scenario scenario : scenarios) {
            scenario.removeActionsForDevice(device);
        }
    }

    public void changeRoomName(Room room, String name) {
        room.setName(name);
    }

    public void addDevice(Room room, SmartDevice device) {
        room.addDevice(device);
    }

    public void removeDevice(SmartDevice device, Room oldRoom) {
        oldRoom.removeDevice(device);
        cleanUpScenariosForDevice(device);
    }

    public void changeDeviceRoom(SmartDevice device, Room oldRoom, Room newRoom) {
        removeDevice(device, oldRoom);
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