package dk.shift6.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Optional;

class TetrisShape {
    public boolean[][] shape;
    Board board;
    Block[] blocks;
    public Color color;
    public Status status;
    public boolean locked;
    private int x, y;

    public enum Status {
        QUEUED,
        MOVING,
        COLLISION,
        COLLISION_AND_NOT_BOARD,
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
    TetrisShape(boolean[][] inputShape, Board board, int x, int y, Color color) {
        shape = inputShape;
        this.x = x;
        this.y = y;
        Block[][] blockShape = new Block[shape.length][shape[0].length];
        //initialize blocks
        ArrayList<Block> _blocks = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int _x = 0; _x < shape[i].length; _x++) {
                if (shape[i][_x]) {
                    Block newBlock = new Block(this,_x + x, i - shape.length);
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

    public Status moveShape(Directions direction, Boolean collisionTestDone) throws ShapeLockedException {
        if(this.locked) throw new ShapeLockedException("This shape is locked");

        //test for collision which will set futureX/Y if no collision
        if(collisionTestDone == null || !collisionTestDone) {
            for (Block b : this.blocks) {
                if (b.collisionTest(direction)) { //will break if collisionTest has already been done i.e. rotate
                    if (b.y < 0)
                        return this.status = Status.COLLISION_AND_NOT_BOARD;
                    else
                        return direction == Directions.DOWN? (this.status = Status.COLLISION) : (this.status = Status.MOVING);

                }
            }
        }
        //clear current placement
        for (Block b : this.blocks) {
            if (b.y >= 0) board.rows[b.y].blocks[b.x] = null;
        }

        //add new placement
        for (Block b : this.blocks) {
            b.commitNewPoint();
            if (b.y >= 0) board.rows[b.y].blocks[b.x] = b;
            this.x = b.x < this.x ? b.x : this.x;
            this.y = b.y > this.y ? b.y : this.y;
        }

        if(direction == Directions.ROTATE) {
            for(Block b: blocks) b.findNeighbours();
        }

        return this.status = Status.MOVING;
    }

    public void rotateShape() {
        if(this.shape.length > 0) this.rotateShape(this.shape);
    }

    public void rotateShape(boolean[][] baseShape) throws IllegalArgumentException {
        if(baseShape.length == 0) throw new IllegalArgumentException("Length of parameter 'baseShape' must be larger than 0");
        int rows = baseShape.length;
        int cols = baseShape[0].length;

        // Create a new matrix for the transposed result
        boolean[][] transposedMatrix = new boolean[cols][rows];

        // Transpose the matrix
        boolean rotationSucceeded = true;
        for (int i = 0, blockIdx = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedMatrix[j][rows-1-i] = this.shape[i][j]; //inserting and reversing the column in one go
                if( transposedMatrix[j][rows-1-i] &&
                    this.blocks[blockIdx++].collisionTestPoint(this.x+(rows - 1 - i), this.y-(cols-1-j))) {             //check new position on board, abort if occupied
                    //mark occupied and move on to next rotation
                    rotationSucceeded = false;
                }
            }
        }
        //if occupied move on to next rotation
        if(!rotationSucceeded) {
            for(Block b: this.blocks) {
                b.regretNewPoint();
            }
        } else {
            this.shape = transposedMatrix;
        }

        //if success move blocks. It shouldn't matter which block ends up where (do I assume somewhere that block 0 is the first block?

    }

}
