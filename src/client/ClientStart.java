package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientStart extends Application {
    /**
     * Startet die Application und führt die "Start"-Fenster aus
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI/Start/start.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Sebo Schiffe versenken");

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
