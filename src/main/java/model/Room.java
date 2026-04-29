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
    private transient List<RoomObserver> roomObservers = new ArrayList<>();

    public Room(String name) {
        this.name = name;
        this.roomObservers = new ArrayList<>();
    }

    public void addDevice(AbstractDevice abstractDevice) {
        if (!abstractDevices.contains(abstractDevice)) {
            abstractDevices.add(abstractDevice);
            notifyObservers();
        }
    }

    /// todo: vllt ne methode public List<AbstractDevice> devicesList() {return abstractDevices} ?

    public void removeDevice(AbstractDevice abstractDevice) {
        abstractDevices.remove(abstractDevice);
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