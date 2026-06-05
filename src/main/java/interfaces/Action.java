package interfaces;

public interface Action {
    //Beschreibung der ausgewaehlten Aktion
    String getDescription();

    String getName();

    //gibt an, dass jede Action "execute" implementieren muss
    void execute();

    void undo();
}
