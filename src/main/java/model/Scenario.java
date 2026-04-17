package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Scenario implements Action {

    private String name;
    private String description;

    private List<Action> actions = new ArrayList<>();

    public Scenario(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addAction(Action action) {
        /// todo: soll man die selbe action mehrfach hinzufügen können?
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
            action.execute();
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