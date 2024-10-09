package dk.shift6.tetris;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED); // or StageStyle.TRANSPARENT



        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("game-board.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 200, 500);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }



    public static void main(String[] args) {
        launch();
    }
}