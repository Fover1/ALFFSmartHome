package org.startmenu;

import atlantafx.base.theme.CupertinoDark;
import controller.SmartHomeAppController;
import frontendController.SmartHomeMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

import static lang.ErrorMessages.MAINVIEW_NOT_FOUND;

public class SmartHomeApp extends Application {

    public static void main(String[] args) {
        launch(args); //Startet die JAVAFX Anwendung
    }

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        SmartHomeAppController logicController = new SmartHomeAppController();

        URL fxmlLocation = getClass().getResource("/MainView.fxml");
        if (fxmlLocation == null) {
            System.err.println(MAINVIEW_NOT_FOUND);
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        SmartHomeMainController guiController = loader.getController();

        guiController.setController(logicController);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Smart Home Szenario-Editor");
        stage.setScene(scene);
        stage.show();
    }
}