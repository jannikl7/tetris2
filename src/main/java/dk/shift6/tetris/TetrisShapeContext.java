package dk.shift6.tetris;

import javafx.scene.paint.Color;

public class TetrisShapeContext {
    boolean[][] shape;
    TetrisShape[][] board;
    int x,  y;
    Color color;

    TetrisShapeContext(boolean[][] shape, int x, int y, Color color) {
            this.shape = shape;
            this.board = board;
            this.x = x;
            this.y = y;
            this.color = color;

    }
}
