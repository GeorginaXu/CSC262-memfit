package edu.smith.cs.csc262.memfit;

import java.util.Comparator;

public class SortBlockByOffset implements Comparator<Block> {

    @Override
    public int compare(Block leftBlock, Block rightBlock) {
        return Integer.compare(leftBlock.getOffset(),rightBlock.getOffset());
    }
}
