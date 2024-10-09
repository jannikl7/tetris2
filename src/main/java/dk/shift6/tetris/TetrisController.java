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

import java.util.ArrayList;
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
    private boolean gameOver = false;

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

                    if(update(deltaSeconds)) // Use a delta time of 1 second (or whatever your update logic needs)
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
        // Clear the canvas
        if(this.gameOver) {
            gc.setFill(Color.color(0.7, 0.7, 0.7, 0.5));
            gc.fillRect(40, 180, 120, 40);
            gc.setFill(Color.DARKRED);
            gc.setFont(new javafx.scene.text.Font(20));  // Set font size
            gc.fillText("GAME OVER!", 50, 200);  // (text, x, y)
        } else {
            gc.setFill(Color.web("#211f1e"));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            for (int boardRow = 0; boardRow < board.length; boardRow++) {
                if(rowStatus[boardRow] == RowStatus.FULL) { //full row ready for clearing
                    board[boardRow] = new TetrisShape[board[boardRow].length];
                    rowStatus[boardRow] = RowStatus.READY_FOR_REMOVAL;
                    //give points
                } else if(rowStatus[boardRow] == RowStatus.READY_FOR_REMOVAL && activeShape.status == TetrisShape.Status.QUEUED) {
                    //remove the row and add one at top
                    //loop back from boardRow and shift to the right
                    for(int i = boardRow; i > 0; i++) { //This might ruin if shape is partly in.
                        board[i] = board[i-1];
                    }
                    //add clear row to the beginning
                    board[0] = new TetrisShape[board[0].length];
                }
                rowStatus[boardRow] = RowStatus.FULL;
                for (int shape = 0; shape < board[boardRow].length; shape++) {
                    activeShape.status = TetrisShape.Status.MOVING;
                    TetrisShape tShape = board[boardRow][shape];
                    if (tShape != null) {
                        if(tShape.status == TetrisShape.Status.COLLISION && boardRow == 0) this.gameOver = true;
                        gc.setFill(tShape.color);
                        gc.fillRect(shape * shapeWidth + 1, boardRow * shapeHeight + 1, shapeWidth - 1, shapeHeight - 1);
                    } else {
                        rowStatus[boardRow] = RowStatus.NONE;
                    }
                }
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
                     this.activeShape.moveShape(board, TetrisShape.Directions.RIGHT);
                     break;
                 case LEFT:
                     this.keyEvent = null;
                     this.activeShape.moveShape(board, TetrisShape.Directions.LEFT);
                     break;
             }
        }
        if (deltaTime >= 0.3) {
            TetrisShape.Status result = this.activeShape.moveShape(board, TetrisShape.Directions.DOWN);
            if( result == TetrisShape.Status.COLLISION) { //at the end of the board or shape is blocking
                //instantiate new shape
                this.activeShape = randomShape();
                this.eventBlocked = false;
            } else if(result == TetrisShape.Status.GAME_OVER) {
                this.gameOver = true;
            }
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

