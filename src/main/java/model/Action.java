package model;

//TODO Frage: Wollen wir einen eigenen Ordner für die ganzen Interfaces anlegen? --> Alex sagt ja, was sagt Finn?

public interface Action {
    //Beschreibung der ausgewaehlten Aktion
    String getDescription();

    String getName();

    //gibt an, dass jede Action "execute" implementieren muss
    void execute();

    void undo();
}
