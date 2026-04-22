package org.example;

import atlantafx.base.theme.CupertinoDark;
import controller.SmartHomeAppController;
import frontendController.SmartHomeMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SmartHomeApp extends Application {

    public static void main(String[] args) {
        launch(args); // Startet die JAVAFX Anwendung
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        SmartHomeAppController logicController = new SmartHomeAppController();

        URL fxmlLocation = getClass().getResource("/MainView.fxml");
        if (fxmlLocation == null) {
            System.err.println("Fehler: MainView.fxml wurde nicht gefunden! " +
                    "Stelle sicher, dass sie in src/main/resources liegt.");
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        SmartHomeMainController guiController = loader.getController();

        guiController.setController(logicController);

        ///  todo: nochmal nachschauen was diese Zeile kann (bzw. das was man übergibt)
        Scene scene = new Scene(root, 1000, 700); // Etwas größer für die moderne Optik
        primaryStage.setTitle("Smart Home Szenario-Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}