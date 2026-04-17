package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {
    private String name;

    private List<AbstractDevice> abstractDevices = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public void addDevice(AbstractDevice abstractDevice) {
        if (!abstractDevices.contains(abstractDevice)) {
            abstractDevices.add(abstractDevice);
        }
    }

    public void removeDevice(AbstractDevice abstractDevice) {
        abstractDevices.remove(abstractDevice);
    }
}