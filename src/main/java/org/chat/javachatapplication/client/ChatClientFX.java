package org.chat.javachatapplication.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatClientFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        //connecting/linking to an application.css file  & applyCSSonMe.fxml file


//        Parent root= FXMLLoader.load((Objects.requireNonNull(getClass().getResource("applyCSSonMe.fxml"))));
//        Scene scene = new Scene(root);
//
//        String css=getClass().getResource("application.css").toExternalForm();
//
//        scene.getStylesheets().add(css);
//        stage.setScene(scene);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chat/javachatapplication/chat-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("JavaFX Chat Client");
        

        String css = getClass().getResource("/org/chat/javachatapplication/stylesheet.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
