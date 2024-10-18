package dk.shift6.tetris;

import java.util.Arrays;

public class Row {
    Block[] blocks;
    RowState state;
    private int size;

    public enum RowState {
        OPEN,
        FULL,
        READY_FOR_REMOVAL
    }

    public Row(int size) {
        this.blocks = new Block[size];
        this.clear();
        this.size = size;
    }

    public void clear() {
        Arrays.fill(this.blocks, null);
    }

    public int getSize() {
        return this.size;
    }
}
