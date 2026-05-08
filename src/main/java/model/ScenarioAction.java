package model;

import controller.SmartHomeAppController;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ScenarioAction implements Action {
    /// todo: ScenarioAction und DeviceAction vergleichen

    //Es sollte nur der Name des Szenarios gespeichert werden
    //unteranderem Wichtig, um keine Endlosschleife in der Json-Datei zu erzeugen
    private UUID targetScnearioID;

    private transient Scenario targetScenario;

    //diese Leeren Konstruktoren werden für Gson benötigt
    public ScenarioAction() {
    }

    public ScenarioAction(UUID targetScnearioID0) {
        this.targetScnearioID = targetScnearioID0;
    }

    private void getTargetScneario() {
        SmartHomeAppController controller = new SmartHomeAppController();
        for (Scenario scenario : controller.getAllScenarios()) {
            if (scenario.getId().equals(targetScnearioID)) {
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
        System.out.println("TargetSzenario: " + targetScnearioID.toString());
        targetScenario.execute();
    }

    @Override
    public String getDescription() {
        return targetScenario.getName() + ": " + targetScenario.getDescription();
    }
}