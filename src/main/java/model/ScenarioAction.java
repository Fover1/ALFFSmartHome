package model;

import controller.SmartHomeAppController;
import lombok.Getter;

import java.util.UUID;

import static lang.ErrorMessages.TARGETSZENARIO_NOT_FOUND;

@Getter
public class ScenarioAction implements Action {

    //Es sollte nur der Name des Szenarios gespeichert werden
    //unteranderem wichtig, um keine Endlosschleife in der Json-Datei zu erzeugen
    private UUID targetScenarioID;

    private transient Scenario targetScenario;

    //diese leeren Konstruktoren werden für Gson benoetigt
    public ScenarioAction() {
    }

    public ScenarioAction(UUID targetScnearioID0) {
        this.targetScenarioID = targetScnearioID0;
    }

    private void getTargetScneario() {
        SmartHomeAppController controller = new SmartHomeAppController();
        for (Scenario scenario : controller.getAllScenarios()) {
            if (scenario.getId().equals(targetScenarioID)) {
                targetScenario = scenario;
            }
        }
    }

    @Override
    public void execute() {
        if (targetScenario == null) {
            getTargetScneario();
        }
        targetScenario.execute();
    }

    @Override
    public void undo() {
        if (targetScenario == null) {
            getTargetScneario();
        }

        if (targetScenario != null) {
            targetScenario.undo();
        } else {
            System.err.println(TARGETSZENARIO_NOT_FOUND);
        }
    }

    @Override
    public String getDescription() {
        return targetScenario.getName() + ": " + targetScenario;
    }
}