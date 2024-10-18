package dk.shift6.tetris;


import java.util.stream.IntStream;

public class Board {


    public Row[] rows;
    private int rowSize, colSize;

    public Board(int rows, int cols) {
        this.rows = IntStream.range(0, rows)
                .mapToObj(i -> new Row(cols))
                .toArray(Row[]::new);
        this.rowSize = rows;
        this.colSize = cols;
    }

    public int getRowSize() {
        return rowSize;
    }

    public int getColSize() {
        return colSize;
    }


    public boolean isGameOver() {
        return (rows[0] != null && rows[0].state == Row.RowState.FULL);
    }

    public void moveRow(int currentPos, int targetPos) {
        rows[targetPos] = rows[currentPos];
        for(Block b: rows[targetPos].blocks) {
            if(b!= null) b.y = targetPos;
        }
    }

    public void clearTopRow() {
        rows[0] = new Row(colSize);
    }
}
