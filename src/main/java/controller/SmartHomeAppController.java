package controller;

import model.Action;
import model.DeviceAction;
import model.LogEntry;
import model.LogListener;
import model.PersistenceManager;
import model.Room;
import model.Scenario;
import model.SmartDevice;
import model.SmartHomeModel;

import java.util.ArrayList;
import java.util.List;

public class SmartHomeAppController {


    public final List<LogListener> logListeners = new ArrayList<>();
    //verbindung zwischen Model (SmartHomeModel) und fester Datenspeicherung (PersistenceManager)
    //Methoden werden teilweise auch von der GUI abgerufen
    private final SmartHomeModel smartHomeModel;
    private String currentConfigFile;

    public SmartHomeAppController() {
        this.smartHomeModel = new SmartHomeModel();
        loadConfiguration("smarthome_config.json");
    }

    public void save() {
        PersistenceManager.save(smartHomeModel.getRooms(), smartHomeModel.getScenarios());
    }

    public void addRoom(String name) {
        smartHomeModel.addRoom(new Room(name));
        save();
    }

    public void deleteRoom(Room room) {
        smartHomeModel.removeRoom(room);
        save();
    }

    public void changeRoomName(Room room, String name) {
        smartHomeModel.changeRoomName(room, name);
    }

    public void changeDeviceName(SmartDevice device, String name) {
        smartHomeModel.changeDeviceName(device, name);
    }

    public void deleteDevice(SmartDevice device, Room oldRoom) {
        smartHomeModel.removeDevice(device, oldRoom);
    }

    public void changeDeviceRoom(SmartDevice device, Room oldRoom, Room newRoom) {
        smartHomeModel.changeDeviceRoom(device, oldRoom, newRoom);
    }


    public List<SmartDevice> getAllDevices() {
        return smartHomeModel.getAllDevices();
    }

    public List<Room> getAllRooms() {
        return smartHomeModel.getRooms();
    }

    public void addSzenario(Scenario scenario) {
        smartHomeModel.addScenario(scenario);
    }

    public List<Scenario> getAllScenarios() {
        return smartHomeModel.getScenarios();
    }

    public void removeScenario(Scenario scenario) {
        smartHomeModel.removeScenario(scenario);
    }

    /// todo: muss das noch in das model?
    public void executeScenario(Scenario scenario) {
        scenario.execute();
        for (Action action : scenario.getActions()) {


            if (action instanceof DeviceAction) {
                LogEntry entry = new LogEntry(
                        scenario.getName(),
                        ((DeviceAction) action).targetDevice().getName(),
                        action.getDescription(),
                        ((DeviceAction) action).parameter().toString()
                );
                notifyLogListeners(entry);
            }

        }
    }

    public void addLogListener(LogListener logListener) {
        logListeners.add(logListener);
    }

    public void notifyLogListeners(LogEntry logEntry) {
        for (LogListener logListener : logListeners) {
            logListener.onLogEntryCreated(logEntry);
        }
    }

    public void loadConfiguration(String file) {
        PersistenceManager.setFileName(file);
        this.currentConfigFile = file;

        PersistenceManager.SmartHomeData data = PersistenceManager.load(currentConfigFile);
        if (data != null) {
            if (data.rooms != null) {
                smartHomeModel.setRooms(data.rooms);
                System.out.println("Räume werden neu geladen");
            }
            if (data.scenarios != null) {
                smartHomeModel.setScenarios(data.scenarios);
                System.out.println("Scenarien werden neu geladen");
            }
        }

    }
}