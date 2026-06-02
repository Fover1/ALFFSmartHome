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
        System.out.println("Führe Szenario aus: " + name);

        for (Action action : actions) {
            System.out.println("Execute: " + action.getDescription());
            action.execute();
        }
    }

    @Override
    public void undo() {
        System.out.println("Mache Szenario rückgängig: " + name);

        for (int i = actions.size() - 1; i >= 0; i--) {
            Action action = actions.get(i);
            System.out.println("Undo Aktion: " + action.getDescription());
            action.undo();
        }
    }

    @Override
    public String toString() {
        return "Szenario: " + name + " (" + actions.size() + " Aktionen)";
    }

    public int getCount() {
        return actions.size();
    }
}