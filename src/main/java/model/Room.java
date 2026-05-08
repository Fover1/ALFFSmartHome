package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {
    private String name;
    private List<SmartDevice> smartDevices = new ArrayList<>();
    private transient List<RoomObserver> roomObservers = new ArrayList<>();

    public Room(String name) {
        this.name = name;
        this.roomObservers = new ArrayList<>();
    }

    public void addDevice(SmartDevice smartDevice) {
        if (!smartDevices.contains(smartDevice)) {
            smartDevices.add(smartDevice);
            notifyObservers();
        }
    }

    /// todo: vllt ne methode public List<smartDevice> devicesList() {return smartDevices} ?

    public void removeDevice(SmartDevice smartDevice) {
        smartDevices.remove(smartDevice);
        notifyObservers();
    }

    public void addObserver(RoomObserver observer) {
        if (roomObservers == null) {
            roomObservers = new ArrayList<>();
        }
        if (!roomObservers.contains(observer)) {
            roomObservers.add(observer);
        }
    }

    public void removeObserver(RoomObserver observer) {
        if (roomObservers != null) {
            roomObservers.remove(observer);
        }
    }

    private void notifyObservers() {
        if (roomObservers != null) {
            for (RoomObserver observer : roomObservers) {
                observer.onDeviceListChanged(this);
            }
        }
    }
}