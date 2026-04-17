package org.example;

import model.AbstractDevice;
import model.Action;
import model.DeviceAction;
import model.DeviceFactory;
import model.Room;
import model.Scenario;

public class Main {
    public static void main(String[] args) {

        // 1. Räume und Geräte anlegen (mithilfe unserer dynamischen Factory!)
        Room wohnzimmer = new Room("Wohnzimmer");
        Room bad = new Room("Badezimmer");

        AbstractDevice deckenlamp = DeviceFactory.createDevice("Lamp", "L-01", "Deckenleuchte", wohnzimmer);
        AbstractDevice badHeizung = DeviceFactory.createDevice("Heizung", "H-01", "Wandheizung", bad);

        // 2. Einzelne Befehle (Commands) erstellen
        // (Licht im Wohnzimmer aus, Heizung im Bad auf 22 Grad)
        Action lichtAus = new DeviceAction(deckenlamp, "Schalten", false);
        Action heizungAn = new DeviceAction(badHeizung, "Temperatur", 22.0);

        // 3. Das Szenario (Composite) erstellen und Aktionen hinzufügen
        Scenario guteNacht = new Scenario("Abend", "Bereitet das Haus auf die Nacht vor");
        guteNacht.addAction(lichtAus);
        guteNacht.addAction(heizungAn);

        System.out.println("Angelegt: " + guteNacht.getDescription());

        // 4. Der magische Moment: Ein einziger Methodenaufruf steuert das ganze Smart Home!
        // Hier passiert die tatsächliche Ausführung.
        System.out.println("Führe Szenario aus...");
        guteNacht.execute();

        // 5. Überprüfen wir, ob es geklappt hat:
        System.out.println("Zustand Deckenlamp: " + deckenlamp.getCurrentState());
        System.out.println("Zustand Badheizung: " + badHeizung.getCurrentState());
    }
}