# ALFF-SmartHome
Leistungsnachweis für das Modul "Fortgeschrittene Programmierung in Java".
## 1 - Vorbedingungen
Um dieses Projekt auszuführen, werden verschiedene Voraussetzungen benötigt. Stellen Sie sicher, dass alle Voraussetzungen erfüllt sind, bevor Sie fortfahren:
Java Development Kit (JDK): Version 23 (oder neuer) ist installiert.
Eine IDE: Wir empfehlen IntelliJ IDEA, da diese IDE hervorragende Werkzeuge für die Java- und JavaFX-Entwicklung bietet und nativ mit dem hier verwendeten Build-Tool Maven harmoniert.
## 2 - Import in IntelliJ IDEA 
Da das Projekt mit Maven verwaltet wird (erkennbar an der pom.xml), ist der Import in IntelliJ IDEA sehr unkompliziert. Bitte folgen Sie diesen Schritten:
Entpacken Sie die Projektdateien auf Ihrem Computer (falls als ZIP-Datei erhalten).
Öffnen Sie IntelliJ IDEA.
Klicken Sie auf dem Willkommensbildschirm auf "Open" (oder gehen Sie im Menü auf File -> Open...).
Navigieren Sie zu dem Ordner ALFFSmartHome (Abgabeordner --> ALFFSmartHomeSystem --> ALFFSmartHome, der Ordner, der die Datei pom.xml enthält), wählen Sie diesen an und klicken Sie auf "OK".
IntelliJ erkennt automatisch, dass es sich um ein Maven-Projekt handelt. Klicken Sie bei Bedarf auf das Pop-up "Load Maven Project", damit alle Abhängigkeiten automatisch heruntergeladen werden.
Gehen Sie zu File -> Project Structure... und wählen Sie unter Project SDK Ihr installiertes JDK 23 aus.
## 3 - Ausführung und Starten
Das Projekt kann direkt aus IntelliJ heraus gestartet werden.
So starten Sie das Programm:
Navigieren Sie im Projektbaum auf der linken Seite ("Project"-Fenster) zu folgendem Pfad:
src \ main \ java \ org \ startmenu \ Main.java
Öffnen Sie die Klasse Main.
Klicken Sie auf den grünen Play-Button links am Rand direkt neben der Zeile public static void main(String[] args) und wählen Sie "Run 'Main.main()'".
## 4 - Nutzung
Nach dem Start öffnet sich die grafische JavaFX-Benutzeroberfläche der SmartHome-Anwendung. Die Anwendung mit ihren verschiedenen Ansichten (Dashboard, Raumverwaltung, Geräteverwaltung, Szenarien) ist benutzerfreundlich gestaltet. Folgen Sie den Menüpunkten der Navigation, um virtuelle Geräte hinzuzufügen, in Räume einzuordnen und zu testen.
## 5 - Tests und Code Coverage
Das Projekt verfügt über eine Reihe von Unit-Tests, um sicherzustellen, dass die Kernkomponenten fehlerfrei arbeiten.
Um die Tests auszuführen:
- Über IntelliJ: Navigieren Sie im Projektbaum zum Verzeichnis src \ test \ java. Machen Sie einen Rechtsklick auf den Ordner java und wählen Sie "Run 'All Tests".
- Über Maven können ein Testrun mit "mvn clean verify" ausgeführt werden.
- Außerdem muss "mvn clean pmd:pmd" ausgeführt werden (mit Doppelklick auf strg das run anything Fenster aufrufen und diesen Befehl ausführen.
-Anschließend ist die Testcoverage unter target \ site \  index.html zu finden.
- Die weiteren Codeanalysen sind unter target \ reports \ pmd.html zu finden.
