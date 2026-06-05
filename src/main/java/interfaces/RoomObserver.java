package interfaces;

import model.Room;

public interface RoomObserver {
    void onDeviceListChanged(Room room);
}