package dk.shift6.tetris;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static javafx.scene.paint.Color.BLUE;

public class TetrisController {

    @FXML
    private Canvas gameCanvas;  // Reference to the Canvas
    @FXML
    private Button exitButton; // Reference to the exit button
    @FXML
    private VBox root;



    private enum RenderState {
        MOVE_SHAPE,
        CLEAR_FULL_ROWS,
        DELETE_CLEARED_ROWS,
        GAME_OVER,
        CHECK_ROWS;
    }
    private RenderState renderState = RenderState.MOVE_SHAPE;

    private int rowCount = 20;
    private int colCount = 10;
    private Board board = new Board(rowCount,colCount);

    // Game variables
    private double shapeWidth = 20;
    private double shapeHeight = 20;

    private double playerX = 100;
    private double playerY = 100;
    private double playerSpeed = 100; // Speed in pixels per second

    private KeyCode keyEvent;
    private boolean eventBlocked = false;

    TetrisShapeContext[] shapes = {new TetrisShapeContext(
            new boolean[][]{
                    new boolean[]{true, true},
                    new boolean[]{true, true}
            }, 4, -1, BLUE),//square
            new TetrisShapeContext(new boolean[][]{
                    new boolean[]{true},
                    new boolean[]{true},
                    new boolean[]{true},
                    new boolean[]{true}
            }, 4, -1, Color.RED), //stick
            new TetrisShapeContext(new boolean[][]{
                    new boolean[]{true, false},
                    new boolean[]{true, false},
                    new boolean[]{true, false},
                    new boolean[]{true, true}
            }, 4, -1, Color.GREEN), //L-shape
            new TetrisShapeContext(new boolean[][] {
                    new boolean[] {false, true, true},
                    new boolean[] {true, true, false}
            }, 4, -1, Color.PURPLE), //lightning
            new TetrisShapeContext(new boolean[][] {
                    new boolean[] {false, true, false},
                    new boolean[] {true, true, true}
            }, 4, -1, Color.YELLOW),
    };
    private TetrisShape activeShape = randomShape(); // Current active shape

    @FXML
    public void initialize() {
        System.out.println("initialized");
        // Set the key event handler on the Canvas
        gameCanvas.setFocusTraversable(true); // Make sure the Canvas is focusable
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);

        // Request focus for the Canvas
        gameCanvas.requestFocus();

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        // Game loop using AnimationTimer
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastUpdate = 0; // Time of the last
            private long lastVertUpdate = 0;

            @Override
            public void handle(long now) {
                // Check if we have a valid last update time
                if (lastUpdate == 0) {
                    lastUpdate = now; // Initialize lastUpdate with the current time
                    lastVertUpdate = now; // Initialize lastUpdate with the current time
                    return; // Skip the first frame
                }

                // Calculate the time difference in nanoseconds
                long deltaTime = now - lastUpdate;
                double deltaSeconds = deltaTime / 1_000_000_000.0;

                // Check if at least one second (1_000_000_000 nanoseconds) has passed
                if (deltaTime >= 50_000_000) {
                    // Update game state (e.g., move the Tetris shape down)
                    boolean updated = false;
                    long startMeasurement = System.nanoTime(), endMeasurement;

                    if(renderState != RenderState.GAME_OVER && (updated =  update(deltaSeconds))) {// Use a delta time of 1 second (or whatever your update logic needs)
                        lastUpdate = now;
                        render(gc);
                        endMeasurement = System.nanoTime();
                        Long elapsedTime = endMeasurement - startMeasurement;
                        //System.out.println("Elapsed time: " + elapsedTime);
                    } else {
                        // Render the game (draw player, background, etc.)
                        render(gc);
                    }
                    if(updated) {
                        if(renderState == RenderState.MOVE_SHAPE) {
                            try {
                                TetrisShape.Status moveResult = activeShape.moveShape(TetrisShape.Directions.DOWN, null);
                                if(moveResult == TetrisShape.Status.COLLISION) {
                                    renderState = RenderState.CHECK_ROWS;
                                    activeShape = null;
                                } else if (moveResult == TetrisShape.Status.COLLISION_AND_NOT_BOARD)
                                    renderState = RenderState.GAME_OVER;
                            } catch (ShapeLockedException e) {
                            }
                        }
                    }
                }
            }
        };

        // Start the game loop
        gameLoop.start();
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
            this.keyEvent = keyEvent.getCode();
    }

    // Render the game (draw the player and other elements)
    private void render(GraphicsContext gc) {
        if(renderState == RenderState.GAME_OVER) {
            gc.setFill(Color.color(0.7, 0.7, 0.7, 0.5));
            gc.fillRect(40, 180, 120, 40);
            gc.setFill(Color.DARKRED);
            gc.setFont(new javafx.scene.text.Font(20));  // Set font size
            gc.fillText("GAME OVER!", 50, 200);  // (text, x, y)
        } else {
            // Clear the canvas
            gc.setFill(Color.web("#211f1e"));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
            for (int boardRow = 0; boardRow < board.rows.length; boardRow++) {
                Row currRow = board.rows[boardRow];
                if(currRow.state == Row.RowState.READY_FOR_REMOVAL) continue;
                    boolean rowFull = true;
                    for (int blockIdx = 0;blockIdx < currRow.getSize(); blockIdx++) {
                        Block block = currRow.blocks[blockIdx];
                        if (block != null) {
                            gc.setFill(block.parentShape.color);
                            gc.fillRect(blockIdx * shapeWidth + 1, boardRow * shapeHeight + 1, shapeWidth - 1, shapeHeight - 1);
                        } else {
                            rowFull = false;
                        }
                    }
                    currRow.state = (rowFull) ? Row.RowState.FULL : Row.RowState.OPEN;
                }
                // Draw additional elements (background, score, etc.)
            }
        }

    // Update the game state (e.g., move player)
    private boolean update(double deltaTime) {
        if(keyEvent != null) {
             switch(keyEvent) {
                 case RIGHT:
                     this.keyEvent = null;
                     try {
                         if(this.activeShape != null) this.activeShape.moveShape(TetrisShape.Directions.RIGHT, false);
                     } catch(ShapeLockedException e){}
                     break;
                 case LEFT:
                     this.keyEvent = null;
                     try {
                         if(this.activeShape != null) this.activeShape.moveShape(TetrisShape.Directions.LEFT, false);
                     } catch(ShapeLockedException e){}
                     break;
                 case SPACE:
                     this.keyEvent = null;
                     this.activeShape.rotateShape();
                     try {
                         this.activeShape.moveShape(TetrisShape.Directions.ROTATE, true);
                     } catch (ShapeLockedException e) {}
             }
        }
        if (deltaTime >= 0.1) {
            if(this.activeShape != null) activeShape.locked = true;
            RenderState nextState = null;
            for (int boardRow = 0; boardRow < board.rows.length; boardRow++) {
                Row currRow = board.rows[boardRow];
                if(renderState == RenderState.CHECK_ROWS) {
                    if (currRow.state == Row.RowState.FULL) { //clear the row and mark it for removal in the next render
                        currRow.clear();
                        currRow.state = Row.RowState.READY_FOR_REMOVAL;
                        nextState = RenderState.DELETE_CLEARED_ROWS;
                    }
                } else if (currRow.state == Row.RowState.READY_FOR_REMOVAL && renderState == RenderState.DELETE_CLEARED_ROWS) {
                    for (int i = boardRow; i > 0; i--) { //shift above rows down by 1
                        board.moveRow(i - 1, i);
                    }
                    board.clearTopRow(); //add clear row to the beginning
                    }
            }
            this.renderState = nextState == null ? RenderState.MOVE_SHAPE : nextState;

            if( activeShape == null && renderState == RenderState.MOVE_SHAPE) {//at the end of the board or shape is blocking
                //instantiate new shape
                this.activeShape = randomShape();
               // TetrisShapeContext context = shapes[0];
               // this.activeShape = new TetrisShape(context.shape, board, context.x, context.y, randomColor());
                this.eventBlocked = false;
            }
            if(this.activeShape != null) this.activeShape.locked = false;

            return true;
        }
        return false;
    }

    @FXML
    private void handleExitButtonClick() {
        Platform.exit();
    }

    private TetrisShape randomShape() {
        TetrisShapeContext context = shapes[ThreadLocalRandom.current().nextInt(shapes.length)];
        return new TetrisShape(context.shape, board, context.x, context.y, context.color);
    }

    Color randomColor() {
        Random random = new Random();

        // Generate random values for red, green, and blue (between 0 and 1)
        double red = random.nextDouble();
        double green = random.nextDouble();
        double blue = random.nextDouble();

        // Create a random Color object
        Color randomColor = new Color(red, green, blue, 1.0); // 1.0 is full opacity
        return randomColor;
    }


}

