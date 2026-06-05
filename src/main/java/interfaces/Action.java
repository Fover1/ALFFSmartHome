package interfaces;

public interface Action {
    //Beschreibung der ausgewaehlten Aktion
    String getDescription();

    //gibt an, dass jede Action "execute" implementieren muss
    void execute();

    void undo();
}
