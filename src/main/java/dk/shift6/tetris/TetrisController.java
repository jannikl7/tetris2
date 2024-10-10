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

import java.util.concurrent.ThreadLocalRandom;

import static javafx.scene.paint.Color.BLUE;

public class TetrisController {

    @FXML
    private Canvas gameCanvas;  // Reference to the Canvas
    @FXML
    private Button exitButton; // Reference to the exit button
    @FXML
    private VBox root;

    private enum RowStatus {
        NONE,
        FULL,
        READY_FOR_REMOVAL
    }

    private enum RenderState {
        MOVE_SHAPE,
        CLEAR_FULL_ROWS,
        DELETE_CLEARED_ROWS,
        GAME_OVER
    }
    private RenderState renderState = RenderState.MOVE_SHAPE;


    private TetrisShape[][] board = new TetrisShape[20][10];
    private RowStatus[] rowStatus = new RowStatus[board.length];

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

                    if(renderState == RenderState.MOVE_SHAPE && update(deltaSeconds)) // Use a delta time of 1 second (or whatever your update logic needs)
                        lastUpdate = now;

                    // Render the game (draw player, background, etc.)
                    render(gc);

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

                for (int boardRow = 0; boardRow < board.length; boardRow++) {
                    if (rowStatus[boardRow] == RowStatus.FULL) { //clear the row and mark it for removel in the next render
                        board[boardRow] = new TetrisShape[board[boardRow].length];
                        rowStatus[boardRow] = RowStatus.READY_FOR_REMOVAL;
                    } else if(rowStatus[boardRow] == RowStatus.READY_FOR_REMOVAL && this.activeShape.status == TetrisShape.Status.COLLISION) {
                        for (int i = boardRow; i > 0; i--) { //shift above rows down by 1
                            board[i] = board[i - 1];
                        }

                        board[0] = new TetrisShape[board[0].length]; //add clear row to the beginning
                        rowStatus[boardRow] = RowStatus.NONE;
                        this.renderState = RenderState.MOVE_SHAPE;
                    }

                    if (rowStatus[boardRow] != RowStatus.READY_FOR_REMOVAL) rowStatus[boardRow] = RowStatus.FULL;

                    boolean rowFull = true;
                    rowStatus[boardRow] = RowStatus.FULL;
                    for (int shape = 0; shape < board[boardRow].length; shape++) {
                        TetrisShape tShape = board[boardRow][shape];
                        if (tShape != null) {
                            gc.setFill(tShape.color);
                            gc.fillRect(shape * shapeWidth + 1, boardRow * shapeHeight + 1, shapeWidth - 1, shapeHeight - 1);
                        } else {
                            rowStatus[boardRow] = RowStatus.NONE;
                            rowFull = false;
                        }
                    }
                    if(rowFull && this.activeShape.status == TetrisShape.Status.COLLISION) renderState = RenderState.CLEAR_FULL_ROWS;
                }
                this.activeShape.locked = false;
                // Draw additional elements (background, score, etc.)
            }
        }

    // Update the game state (e.g., move player)
    private boolean update(double deltaTime) {
        if(keyEvent != null) {
             switch(keyEvent) {
                 case RIGHT:
                     this.keyEvent = null;
                     try {this.activeShape.moveShape(TetrisShape.Directions.RIGHT);} catch(ShapeLockedException e){}
                     break;
                 case LEFT:
                     this.keyEvent = null;
                     try {this.activeShape.moveShape(TetrisShape.Directions.LEFT);} catch(ShapeLockedException e){}
                     break;
                /* case SPACE:
                     this.keyEvent = null;
                     this.activeShape.rotateShape();*/
             }
        }
        if (deltaTime >= 0.1) {
            if( this.activeShape.status == TetrisShape.Status.COLLISION) { //at the end of the board or shape is blocking
                //instantiate new shape
                //this.activeShape = randomShape();
                TetrisShapeContext context = shapes[0];
                this.activeShape = new TetrisShape(context.shape, board, context.x, context.y, context.color);

                this.eventBlocked = false;
            } else if(this.activeShape.status == TetrisShape.Status.GAME_OVER) {
                this.renderState = RenderState.GAME_OVER;
            }
            try {this.activeShape.moveShape(TetrisShape.Directions.DOWN);} catch(ShapeLockedException e){}
            this.activeShape.locked = true;
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


}

