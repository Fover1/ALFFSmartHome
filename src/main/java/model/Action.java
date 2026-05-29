package model;

public interface Action {
    //Basis, was "getan" werden kann"
    String getDescription();

    //gibt an, dass jede Action nur "ausfürbar" sein muss
    void execute();
}
