package org.example;

import model.AbstractDevice;
import model.Action;
import model.DeviceAction;
import model.DeviceFactory;
import model.Room;
import model.Scenario;

public class Main {
    public static void main(String[] args) {
        Room wohnzimmer = new Room("Wohnzimmer");

        // Gerät erstellen
        AbstractDevice lampe = DeviceFactory.createDevice("Lamp", "L-01", "Deckenlampe", wohnzimmer);

        // 1. Observer anmelden (Simuliert die GUI!)
        lampe.addObserver(device -> {
            System.out.println("[GUI-UPDATE] Gerät '" + device.getName() + "' hat sich geändert! Neuer Zustand: " + device.getCurrentState());
        });

        System.out.println("--- Starte Simulation ---");

        // 2. Szenario erstellen und ausführen
        Action lichtAn = new DeviceAction(lampe, "Schalten", true);
        Scenario abend = new Scenario("Abendmodus", "Schaltet das Licht ein");
        abend.addAction(lichtAn);

        // 3. Ausführen! Wenn alles klappt, sollte die Lampe jetzt automatisch den Print-Befehl unseres Observers auslösen.
        abend.execute();
    }
}