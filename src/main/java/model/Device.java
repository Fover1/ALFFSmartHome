package model;

public abstract class Device {
    private final String id;
    private String name;
    private Room room;

    public Device(String id, String name, Room room) {
        this.id = id;
        this.name = name;
        this.room = room;
    }

    public abstract String getDeviceType();

    public abstract String getCurrentState();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}