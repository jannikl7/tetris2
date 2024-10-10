package dk.shift6.tetris;

import java.util.List;

public class Block {
    int x, y;
    Integer futureX = null;
    Integer futureY = null;
    TetrisShape[][] board;
    Block leftBlock, topBlock, rightBlock, bottomBlock;
    TetrisShape parentShape;


    Block(TetrisShape parentShape, TetrisShape[][] board, int x, int y) {
        this.parentShape = parentShape;
        this.board = board;
        this.x = x;
        this.y = y;
    }

    boolean collisionTest(TetrisShape.Directions directions) {
        int newX = this.x;
        int newY = this.y;
        if(directions == TetrisShape.Directions.DOWN) {
            newY = this.y + 1;
        } else if(directions == TetrisShape.Directions.LEFT) {
            newX = this.x - 1;
        } else if(directions == TetrisShape.Directions.RIGHT) {
            newX = this.x + 1;
        }
        return collisionTestPoint(newX, newY);
    }

    boolean collisionTestPoint(int newX, int newY) {
        if(     (newX < 0 || newX >= board[0].length)
                || newY > board.length -1
                || (newY >= 0 && newX >= 0  && board[newY][newX] != null && board[newY][newX] != this.parentShape ))
            return true;
        futureX = newX;
        futureY = newY;
        return false;
    }

    void commitNewPoint() {
        x = futureX;
        y = futureY;
        futureX = futureY = null;
    }

  /*  boolean collisionTestRight() {
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
    }*/

   /* boolean willCollide(TetrisShape.Directions direction) {
        if (direction == TetrisShape.Directions.LEFT && this.leftBlock == null)
            return collisionTestLeft();
        else if (direction == TetrisShape.Directions.RIGHT && this.rightBlock == null)
            return collisionTestRight();
        else if (direction == TetrisShape.Directions.DOWN && this.bottomBlock == null)
            return collisionTestDown();

        return false; //we do not test the given direction
    }*/

    void move(TetrisShape.Directions direction) {
        if (direction == TetrisShape.Directions.DOWN)
            this.y++;
        else if (direction == TetrisShape.Directions.LEFT)
            this.x--;
        else if (direction == TetrisShape.Directions.RIGHT)
            this.x++;
    }


    public void regretNewPoint() {
        futureY = futureX = null;
    }

    public void setLeftBlock(Block leftBlock) {
        this.leftBlock = leftBlock;
    }

    public void setTopBlock(Block topBlock) {
        this.topBlock = topBlock;
    }

    public void setRightBlock(Block rightBlock) {
        this.rightBlock = rightBlock;
    }

    public void setBottomBlock(Block bottomBlock) {
        this.bottomBlock = bottomBlock;
    }
}
