package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {
    private String name;

    private List<Device> devices = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public void addDevice(Device device) {
        if (!devices.contains(device)) {
            devices.add(device);
        }
    }

    public void removeDevice(Device device) {
        devices.remove(device);
    }
}