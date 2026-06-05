package controller;

import interfaces.Action;
import model.DeviceAction;
import model.LogEntry;
import interfaces.LogListener;
import model.PersistenceManager;
import model.Room;
import model.Scenario;
import interfaces.SmartDevice;
import model.SmartHomeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static lang.ErrorMessages.NO_ACTION_TO_UNDO;

//Verbindung zwischen Model (SmartHomeModel) und fester Datenspeicherung (PersistenceManager)
//Methoden werden teilweise auch von der GUI abgerufen
public class SmartHomeAppController {
    public final List<LogListener> logListeners = new ArrayList<>();
    private final SmartHomeModel smartHomeModel;
    private final Stack<Action> actionHistory = new Stack<>();
    private String currentConfigFile;

    public SmartHomeAppController() {
        this.smartHomeModel = new SmartHomeModel();
    }

    public void save() {
        PersistenceManager.save(smartHomeModel.getRooms(), smartHomeModel.getScenarios());
    }

    public void addRoom(String name) {
        smartHomeModel.addRoom(new Room(name));
        save();
    }

    public void deleteRoom(Room room) {
        for (SmartDevice device : room.getSmartDevices()) {
            removeDeviceFromAllScenarios(device);
        }
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
        removeDeviceFromAllScenarios(device);
        smartHomeModel.removeDevice(device, oldRoom);
        save();
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

        //Wenn es eine manuelle Geraeteaktion war, wird es ins Log geschrieben
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
            //TODO: Frage: Ist es so gewollt, dass jede Aktion "System" und "Undo" angezeigt wird, wenn sie rückgängig gemacht wird? --> Sollten wir nochmal drüber sprechen
            notifyLogListeners(new LogEntry("System", "Undo", lastAction.getDescription(), "Aktion rückgängig gemacht"));
        } else {
            System.out.println(NO_ACTION_TO_UNDO);
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
            }
            if (data.scenarios != null) {
                smartHomeModel.setScenarios(data.scenarios);
            }
        }
    }

    private void removeDeviceFromAllScenarios(SmartDevice device) {
        List<Scenario> emptyScenarios = new ArrayList<>();

        for (Scenario scenario : smartHomeModel.getScenarios()) {
            removeDeviceFromScenario(scenario, device);
            if (scenario.getActions().isEmpty()) {
                emptyScenarios.add(scenario);
            }
        }
        smartHomeModel.getScenarios().removeAll(emptyScenarios);
    }

    private void removeDeviceFromScenario(Scenario scenario, SmartDevice device) {
        scenario.getActions().removeIf(action -> {
            if (action instanceof DeviceAction deviceAction) {
                return deviceAction.getTargetDevice().getId().equals(device.getId());
            } else if (action instanceof Scenario nestedScenario) {
                removeDeviceFromScenario(nestedScenario, device);
                return nestedScenario.getActions().isEmpty();
            }
            return false;
        });
    }
}