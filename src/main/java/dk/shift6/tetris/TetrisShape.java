package dk.shift6.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

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

    class Block {
        int x, y;
        TetrisShape[][] board;

        List<Directions> sensitiveDirections;

        Block(TetrisShape[][] board, List<Directions> sensitiveDirections, int x, int y) {
            this.board = board;
            this.sensitiveDirections = sensitiveDirections;
            this.x = x;
            this.y = y;
        }

        boolean collisionTestRight() {
            System.out.println("moveRight");
            //is shape already at edge or has a neighbour?
            if (this.y >= 0 && (this.x + 1 == board[this.y].length || board[this.y][this.x + 1] != null))
                return true;

            return false;
        }

        boolean collisionTestLeft() {
            //at the edge
            if (this.y >= 0 && (this.x == 0 || board[this.y][this.x - 1] != null))
                return true;

            return false;
        }

        boolean collisionTestDown() {
            if (this.y + 1 >= 0 && (this.y + 1 >= board.length || board[this.y + 1][this.x] != null)) {
                return true;
            }
            return false;
        }

        boolean willCollide(Directions direction) {
            if (direction == Directions.LEFT && this.sensitiveDirections.contains(Directions.LEFT))
                return collisionTestLeft();
            else if (direction == Directions.RIGHT && this.sensitiveDirections.contains(Directions.RIGHT))
                return collisionTestRight();
            else if (direction == Directions.DOWN && this.sensitiveDirections.contains(Directions.DOWN))
                return collisionTestDown();

            return false; //we do not test the given direction
        }

        void move(Directions direction) {
            if (direction == Directions.DOWN)
                this.y++;
            else if (direction == Directions.LEFT)
                this.x--;
            else if (direction == Directions.RIGHT)
                this.x++;
        }


    }
}
