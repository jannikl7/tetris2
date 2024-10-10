package dk.shift6.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;

class TetrisShape {
    public boolean[][] shape;
    TetrisShape[][] board;
    Block[] blocks;
    public Color color;
    public Status status;
    public boolean locked;

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
        ROTATE,
    }

    /**
     * @param inputShape A two-dimensional array describing the shape
     * @param x
     * @param y
     */
    TetrisShape(boolean[][] inputShape, TetrisShape[][] board, int x, int y, Color color) {
        shape = inputShape;
        Block[][] blockShape = new Block[shape.length][shape[0].length];
        //initialize blocks
        ArrayList<Block> _blocks = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int _x = 0; _x < shape[i].length; _x++) {
                if (shape[i][_x]) {
                    Block newBlock = new Block(this, board,_x + x, i - shape.length);
                    if(_x > 0 && _blocks.size() > 0) {
                        Block leftBlock = _blocks.get(_blocks.size()-1);
                        if(leftBlock != null) {
                            newBlock.setLeftBlock(leftBlock);
                            leftBlock.setRightBlock(newBlock);
                        }
                    }
                    if(i > 0) {
                        Block topBlock = _blocks.get(_blocks.size()-shape[i].length);
                        if(topBlock != null) {
                            newBlock.setTopBlock(topBlock);
                            topBlock.setBottomBlock(newBlock);
                        }
                    }
                    _blocks.add(newBlock);
                } else {
                    _blocks.add(null);
                }
            }
        }

        this.blocks = _blocks.stream().filter(e -> e != null).toArray(Block[]::new);
        this.color = color;
        this.status = Status.QUEUED;
        this.board = board;
    }

    public Status moveShape(Directions direction) throws ShapeLockedException {
        if(this.locked) throw new ShapeLockedException("This shape is locked");
        for (Block b : this.blocks) {
            if (b.collisionTest(direction)) {
                return this.status = Status.COLLISION;
            }
        }
        //clear current placement
        for (Block b : this.blocks) {
            if (b.y >= 0) board[b.y][b.x] = null;
        }

        //add new placement
        for (Block b : this.blocks) {
            b.commitNewPoint();
            if (b.y >= 0) board[b.y][b.x] = this;
        }

        return Status.MOVING;
    }

    public void rotateShape() {
        boolean rotationSucceeded = false;
        int attempts = 0;
        while(!rotationSucceeded || attempts == 3) {
            attempts++;
            Integer baseY = null, baseX = null;
            for(int i = 0; i < this.blocks.length; i++) {
                Block b = this.blocks[i];
                //find new position
                int newY = (baseX == null) ? baseX = b.x : baseX;
                int newX = shape.length - 1 - (baseY == null ? baseY = b.y : baseY);
                //if Block collides try next position
                if (!b.collisionTestPoint(newX, newY))
                    break;
                else if(i == this.blocks.length - 1)
                    rotationSucceeded = true;
            }
        }

        if(!rotationSucceeded) {
            for(Block b: this.blocks) {
                b.regretNewPoint();
            }
        }
    }

}
