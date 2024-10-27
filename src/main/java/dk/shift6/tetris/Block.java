package dk.shift6.tetris;

import java.util.List;

public class Block {
    int x, y;
    Integer futureX = null;
    Integer futureY = null;
    Block leftBlock, topBlock, rightBlock, bottomBlock;
    TetrisShape parentShape;


    Block(TetrisShape parentShape, int x, int y) {
        this.parentShape = parentShape;
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

    /**
     * return true if there is a collision at the given point
     * @param newX
     * @param newY
     * @return
     */
    boolean collisionTestPoint(int newX, int newY) {
        if( (newX < 0 || newX >= this.parentShape.board.rows[0].blocks.length) ||
            newY > this.parentShape.board.getRowSize() -1 ||
            (newY >= 0 && this.parentShape.board.rows[newY].blocks[newX] != null &&
             this.parentShape.board.rows[newY].blocks[newX].parentShape != this.parentShape))
            return true;
        futureX = newX;
        futureY = newY;
        return false;
    }

    void commitNewPoint() {
        x = futureX; //her var en nullpointer på futureX efter et spacebar input?
        y = futureY;
        futureX = futureY = null;
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

    public void findNeighbours() {
        for(Block b: this.parentShape.blocks) {
            if(b.y == this.y-1 && b.x == this.x) this.setTopBlock(b);
            if(b.y == this.y+1 && b.x == this.x) this.setBottomBlock(b);
            if(b.x == this.x-1 && b.y == this.y) this.setLeftBlock(b);
            if(b.x == this.x+1 && b.y == this.y) this.setRightBlock(b);
        }
    }
}
