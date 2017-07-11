package datatools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainpage.fxml"));
        primaryStage.setTitle("RESTful Association Grabber");
        primaryStage.setScene(new Scene(root, 805, 675));
        primaryStage.show();
        MainpageController._stage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
