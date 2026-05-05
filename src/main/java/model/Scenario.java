package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Scenario implements Action {

    //Ein Scenario ist eigentlich auch eine Action, in welcher einfach bei execut mehrere Actions aufgerufen werden
    //Außerdem werden weitere Methoden zur Verwaltung mehrerer Actions implementiert

    private UUID id;
    private String name;
    private String description;
    private List<Action> actions = new ArrayList<>();

    /// todo: muss man den durchreichen oder kann man den immer neu machen?
//    private SmartHomeAppController smarthomeAppController = new SmartHomeAppController();
    public Scenario(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
    }

    public void addAction(Action action) {
        if (!actions.contains(action)) {
            actions.add(action);
        }
    }

    public void removeAction(Action action) {
        actions.remove(action);
    }

    @Override
    public void execute() {
        for (Action action : actions) {
            System.out.println("Execute " + action.toString());

            if (action instanceof ScenarioAction) {
                System.out.println("Execute ScenarioAction");
                action.execute();

            } else if (action instanceof DeviceAction(SmartDevice device, String functionName, Object parameter)) {
                System.out.println("Execute DeviceAction");

                AbstractDevice targetDevice = (AbstractDevice) device;

                if (targetDevice != null) {
                    targetDevice.executeFunction(functionName, parameter);
                } else {
                    System.err.println("Fehler: Kein Zielgerät in der Aktion definiert.");
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Szenario: " + name + " (" + actions.size() + " Aktionen)";
    }

    public int getCount() {
        return actions.size();
    }
}