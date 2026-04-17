package org.example;

import model.AbstractDevice;
import model.Action;
import model.DeviceAction;
import model.DeviceFactory;
import model.Room;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.print("Hello and welcome!");

        Room wohnzimmer = new Room("Wohnzimmer");
        AbstractDevice lampe = DeviceFactory.createDevice("Lamp", "1", "Deckenleuchte", wohnzimmer);

        // 2. Wir erstellen die Aktionen (die "Aufgabenzettel")
        Action aktion1 = new DeviceAction(lampe, "Schalten", true);
        Action aktion2 = new DeviceAction(lampe, "Helligkeit", 75);

        System.out.println("Aktion angelegt: " + aktion2.getDescription());
        // Bisher ist noch nichts passiert! Der Zustand der Lampe ist noch unverändert.

        // 3. Später in der App: Der Nutzer drückt auf "Ausführen"
        aktion1.execute();
        aktion2.execute();

        System.out.println("Neuer Zustand: " + lampe.getCurrentState());

    }
}