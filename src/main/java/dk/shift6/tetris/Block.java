package dk.shift6.tetris;

import java.util.List;

public class Block {
    int x, y;
    TetrisShape[][] board;

    List<TetrisShape.Directions> sensitiveDirections;

    Block(TetrisShape[][] board, List<TetrisShape.Directions> sensitiveDirections, int x, int y) {
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

    boolean willCollide(TetrisShape.Directions direction) {
        if (direction == TetrisShape.Directions.LEFT && this.sensitiveDirections.contains(TetrisShape.Directions.LEFT))
            return collisionTestLeft();
        else if (direction == TetrisShape.Directions.RIGHT && this.sensitiveDirections.contains(TetrisShape.Directions.RIGHT))
            return collisionTestRight();
        else if (direction == TetrisShape.Directions.DOWN && this.sensitiveDirections.contains(TetrisShape.Directions.DOWN))
            return collisionTestDown();

        return false; //we do not test the given direction
    }

    void move(TetrisShape.Directions direction) {
        if (direction == TetrisShape.Directions.DOWN)
            this.y++;
        else if (direction == TetrisShape.Directions.LEFT)
            this.x--;
        else if (direction == TetrisShape.Directions.RIGHT)
            this.x++;
    }


}
