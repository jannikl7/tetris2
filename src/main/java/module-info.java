module dk.shift6.tetris {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens dk.shift6.tetris to javafx.fxml;
    exports dk.shift6.tetris;
}