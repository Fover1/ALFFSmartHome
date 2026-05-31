package model;

//TODO Frage: Wollen wir einen eigenen Ordner für die ganzen Interfaces anlegen?

public interface Action {
    //Basis, was "getan" werden kann //TODO Frage: Was getan werden kann? Ist das nicht einfach die Beschreibung, was die Geräte können? Also was die Funktionen sind?
    String getDescription();

    //gibt an, dass jede Action nur "ausfürbar" sein muss //TODO: Kommentar überarbeiten
    void execute();

    void undo();
}
