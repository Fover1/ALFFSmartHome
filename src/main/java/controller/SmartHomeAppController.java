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
import java.util.Stack;

public class SmartHomeAppController {


    public final List<LogListener> logListeners = new ArrayList<>();
    //verbindung zwischen Model (SmartHomeModel) und fester Datenspeicherung (PersistenceManager)
    //Methoden werden teilweise auch von der GUI abgerufen
    private final SmartHomeModel smartHomeModel;
    private final Stack<Action> actionHistory = new Stack<>();
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

    public void executeAndRemember(Action action) {
        action.execute();
        actionHistory.push(action);

        // Wenn es eine manuelle Geräteaktion war, sofort ins Log schreiben!
        if (action instanceof DeviceAction deviceAction) {
            LogEntry entry = new LogEntry(
                    "Manuell",
                    deviceAction.getTargetDevice().getName(),
                    deviceAction.getDescription(),
                    String.valueOf(deviceAction.getParameter())
            );
            notifyLogListeners(entry);
        }
    }

    public void executeScenario(Scenario scenario) {
        executeAndRemember(scenario);

        for (Action action : scenario.getActions()) {
            if (action instanceof DeviceAction deviceAction) {
                LogEntry entry = new LogEntry(
                        scenario.getName(),
                        deviceAction.getTargetDevice().getName(),
                        action.getDescription(),
                        String.valueOf(deviceAction.getParameter())
                );
                notifyLogListeners(entry);
            }
        }
    }

    public void undoLastAction() {
        if (!actionHistory.isEmpty()) {
            Action lastAction = actionHistory.pop();
            lastAction.undo();

            notifyLogListeners(new LogEntry("System", "Undo", lastAction.getDescription(), "Aktion rückgängig gemacht"));
        } else {
            System.out.println("Keine Aktion zum Rückgängigmachen vorhanden.");
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