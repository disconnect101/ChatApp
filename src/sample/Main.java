package sample;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest(e -> {                      ///// On close eventhandler;
            try {
                controller.closeConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        primaryStage.setTitle("ChatApp");
        primaryStage.setScene(new Scene(root, 800, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
