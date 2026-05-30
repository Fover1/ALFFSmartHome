package model;

import controller.SmartHomeAppController;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ScenarioAction implements Action {

    //Es sollte nur der Name des Szenarios gespeichert werden
    //unteranderem Wichtig, um keine Endlosschleife in der Json-Datei zu erzeugen
    private UUID targetScenarioID;

    private transient Scenario targetScenario;

    //diese Leeren Konstruktoren werden für Gson benötigt
    public ScenarioAction() {
    }

    public ScenarioAction(UUID targetScnearioID0) {
        this.targetScenarioID = targetScnearioID0;
    }

    private void getTargetScneario() {
        SmartHomeAppController controller = new SmartHomeAppController();
        for (Scenario scenario : controller.getAllScenarios()) {
            if (scenario.getId().equals(targetScenarioID)) {
                System.out.println(scenario + " das ist das zenario");
                targetScenario = scenario;
            }
        }
    }

    @Override
    public void execute() {
        if (targetScenario == null) {
            getTargetScneario();
        }
        System.out.println("TargetSzenario: " + targetScenarioID.toString());
        targetScenario.execute();
    }

    @Override
    public void undo() {
        if (targetScenario == null) {
            getTargetScneario();
        }

        if (targetScenario != null) {
            System.out.println("Undo TargetSzenario: " + targetScenarioID.toString());
            targetScenario.undo();
        } else {
            System.err.println("Fehler: TargetSzenario konnte für Undo nicht gefunden werden.");
        }
    }

    @Override
    public String getDescription() {
        return targetScenario.getName() + ": " + targetScenario;
    }
}