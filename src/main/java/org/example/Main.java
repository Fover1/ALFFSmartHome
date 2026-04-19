package org.example;

import model.AbstractDevice;
import model.Action;
import model.DeviceAction;
import model.DeviceFactory;
import model.DeviceObserver;
import model.DeviceScanner;
import model.PersistenceManager;
import model.Room;
import model.Scenario;

import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        System.out.println("=== 1. Device Scanner (Dynamisches Suchen) ===");
        // Sucht im Ordner "devices" nach kompilierten Klassen (Scanner Pattern)
        // Passe den String "devices" an, falls dein Package anders heißt!
        List<String> availableDevices = DeviceScanner.getAllDeviceTypes("devices");
        System.out.println("Gefundene Gerätetypen auf der Festplatte: " + availableDevices);


        System.out.println("\n=== 2. Räume und Geräte anlegen (Factory & Reflection) ===");
        Room wohnzimmer = new Room("Wohnzimmer");
        Room kueche = new Room("Küche");

        // Geräte dynamisch über die Factory erzeugen (Open/Closed Principle)
        AbstractDevice deckenLampe = DeviceFactory.createDevice("Lamp", "L-01", "Deckenlampe", wohnzimmer);
        AbstractDevice dekoLampe = DeviceFactory.createDevice("Lamp", "L-01", "Dekolampe", wohnzimmer);
        AbstractDevice steckdose = DeviceFactory.createDevice("Socket", "S-01", "Kaffeemaschine", kueche);

        // Angenommen, deine Room-Klasse hat eine Liste für Geräte.
        // Falls du sie dort eintragen musst, füge hier z.B. wohnzimmer.addDevice(deckenLampe) hinzu.
        wohnzimmer.getAbstractDevices().add(deckenLampe);
        kueche.getAbstractDevices().add(steckdose);
        wohnzimmer.getAbstractDevices().add(dekoLampe);

        System.out.println("\n=== 3. Observer Pattern (GUI-Simulation) ===");
        // Wir bauen uns einen "Zuhörer", der stellvertretend für die grafische Oberfläche steht
        DeviceObserver guiSimulator = device -> {
            System.out.println("  [GUI-UPDATE] '" + device.getName() + "' hat Zustand gewechselt auf: " + device.getCurrentState());
        };

        // Die Geräte wissen nicht, dass dieser Observer existiert, sie klingeln nur, wenn sie sich ändern!
        deckenLampe.addObserver(guiSimulator);
        steckdose.addObserver(guiSimulator);


        System.out.println("\n=== 4. Direkte Gerätefunktionen (Strategy Pattern) ===");
        System.out.println("Verfügbare Funktionen der Lampe: " + deckenLampe.getAvailableFunctions());
        System.out.println("-> Führe 'Schalten' direkt auf der Lampe aus:");

        // Führt die Lambda-Logik aus und triggert den GUI-Simulator
        deckenLampe.executeFunction("Schalten", true);


        System.out.println("\n=== 5. Aktionen und Szenarien (Command & Composite Pattern) ===");
        // Aktionen als eigenständige Objekte anlegen ("Bestellzettel")
        Action lichtAus = new DeviceAction(deckenLampe, "Schalten", false);
        Action kaffeeAn = new DeviceAction(steckdose, "Schalten", true);

        // Szenario zusammenbauen (Das "Klemmbrett" für die Bestellzettel)
        Scenario morgenRoutine = new Scenario("Guten Morgen", "Bereitet Kaffee vor und macht das Licht aus");
        morgenRoutine.addAction(lichtAus);
        morgenRoutine.addAction(kaffeeAn);

        System.out.println("Szenario angelegt: " + morgenRoutine.getDescription());
        System.out.println("-> Führe Szenario aus...");
        morgenRoutine.execute(); // Triggert alle Befehle und damit auch wieder unsere GUI!


        System.out.println("\n=== 6. Persistenz (Speichern der Konfiguration) ===");
        List<Room> myRooms = Arrays.asList(wohnzimmer, kueche);
        List<Scenario> myScenarios = List.of(morgenRoutine);

        System.out.println("Speichere aktuelles Smart Home als JSON...");
        // Wir nutzen den PersistenceManager mit Gson
        PersistenceManager.save(myRooms, myScenarios);


        System.out.println("\n=== 7. Persistenz (Laden & Wiederbeleben) ===");
        System.out.println("Lade System aus der Datei neu...");
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load();
        System.out.println("Hier angekommen ");

        if (loadedData != null && !loadedData.rooms.isEmpty()) {
            System.out.println("Erfolgreich geladen! Anzahl Räume: " + loadedData.rooms.size());
            System.out.println("Anzahl Szenarien: " + loadedData.scenarios.size());

            // 8. Beweis: Der "Defibrillator" (restoreAfterLoad) funktioniert!
            System.out.println("\n=== 8. Test des geladenen Systems ===");
            // Wir holen uns den ersten Raum und daraus das erste Gerät (die Lampe)
            Room loadedWohnzimmer = loadedData.rooms.get(0);

            // HINWEIS: Passe getAbstractDevices() an, falls dein Getter in Room anders heißt!
            AbstractDevice loadedLampe = loadedWohnzimmer.getAbstractDevices().get(0);

            System.out.println("Geladenes Gerät heißt: " + loadedLampe.getName());
            System.out.println("Zustand direkt nach dem Laden: " + loadedLampe.getCurrentState());

            // Da Observer 'transient' sind, müssen wir nach einem Neustart unsere GUI neu anmelden
            loadedLampe.addObserver(device -> System.out.println("  [NEUE GUI] " + device.getName() + " -> " + device.getCurrentState()));

            System.out.println("-> Führe Funktion auf geladener Lampe aus (Darf nicht abstürzen!):");
            // Wenn das klappt, beweist das, dass deine transienten Lambdas (functions)
            // durch 'restoreAfterLoad' erfolgreich wiederhergestellt wurden!
            loadedLampe.executeFunction("Schalten", true);
        }
    }
}