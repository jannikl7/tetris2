package dk.shift6.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;

class TetrisShape {
    public boolean[][] shape;
    TetrisShape[][] board;
    Block[] blocks;
    public Color color;
    public Status status;

    public enum Status {
        QUEUED,
        MOVING,
        COLLISION,
        GAME_OVER
    }

    enum Directions {
        LEFT,
        RIGHT,
        DOWN,
    }

    /**
     * @param inputShape A two-dimensional array describing the shape
     * @param x
     * @param y
     */
    TetrisShape(boolean[][] inputShape, TetrisShape[][] board, int x, int y, Color color) {
        shape = inputShape;
        //initialize blocks
        ArrayList<Block> _blocks = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int _x = 0; _x < shape[i].length; _x++) {
                if (shape[i][_x]) {
                    ArrayList<Directions> sensitiveDirections = new ArrayList<>();
                    if (_x == 0) sensitiveDirections.add(Directions.LEFT);
                    if (_x == shape[i].length - 1) sensitiveDirections.add(Directions.RIGHT);
                    if (i+1 == shape.length || !shape[i + 1][_x]) sensitiveDirections.add(Directions.DOWN);
                    _blocks.add(new Block(
                            board, sensitiveDirections.stream().toList(), _x + x, i - shape.length)
                    );
                }
            }
        }
        this.blocks = _blocks.toArray(new Block[0]);
        this.color = color;
        this.status = Status.QUEUED;
        this.board = board;
    }


    public Status moveShape(TetrisShape[][] board, Directions direction) {
        for (Block b : this.blocks) {
            if (b.willCollide(direction)) {
                return this.status = Status.COLLISION;
            }
        }
        for (Block b : this.blocks) {
            if (b.y >= 0) board[b.y][b.x] = null;
        }

        for (Block b : this.blocks) {
            b.move(direction);
            if (b.y >= 0) board[b.y][b.x] = this;
        }

        return Status.MOVING;
        //draw shape

        /*
        for(int i = 0; i < shape.length; i++) {
            boolean[] row = shape[shape.length-1-i];
            for(int x = 0; this.y-i >= 0 && x < row.length; x++) {
                if(row[x]) {
                    board[this.y - i][this.x + x] = this;
                }else if(board[this.y - i][this.x + x] == this) {
                    board[this.y - i][this.x + x] = null;
                }
            }
        }*/
    }

}
