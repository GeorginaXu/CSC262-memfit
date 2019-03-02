package edu.smith.cs.csc262.memfit;

public class Block {
    private String name;
    private int size;
    private int offset;

    public Block(String name, int size, int offset) {
        this.name = name;
        this.size = size;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    public void setSize(int newSize) {
        this.size = newSize;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /* This method returns a string that includes the name, offset, and size of the block. */
    public String toString() {
        String block = name + " " + "with offset " + Integer.toString(offset) + " and size " + Integer.toString(size);
        return block;
    }

    /* This method checks whether this block is adjacent to another block. */
    public boolean isAdjacent(Block anotherBlock) {
        if ((anotherBlock.getOffset() == this.offset + this.size) ||
        (this.offset == anotherBlock.getOffset() + anotherBlock.getSize())) {
            return true;
        } else {
            return false;
        }
    }
}
