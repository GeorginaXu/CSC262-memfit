package edu.smith.cs.csc262.memfit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

public class Simulation {

    /**
     * This selected algorithm in the input file. Either first, worst, best, or random.
     */
    String algorithm;

    /**
     * Stores all the available free blocks.
     */
    List<Block> freeList = new ArrayList<>();

    /**
     * Stores all the used blocks.
     */
    List<Block> usedList = new ArrayList<>();

    /**
     * The original pool's size.
     */
    int poolSize = 0;

    /**
     * This is a pointer to the block that is right after the last-allocated block.
     * This is used for next fit algorithm.
     */
    Block nextBlock;

    int failedAlloc = 0;

    /** Getter and setter methods used only for Unit Tests. */
    public String getAlgorithm(){ return algorithm; }
    public void setAlgorithm(String algorithm){ this.algorithm = algorithm; }
    public int getPoolSize(){ return poolSize; }
    public void setPoolSize(int poolSize) { this.poolSize = poolSize;}
    public int getFailedAlloc() { return failedAlloc; }
    public List<Block> getFreeList() { return freeList; }
    public void setFreeList(List<Block> newFreeList) { this.freeList = newFreeList; }
    public List<Block> getUsedList() { return usedList; }
    public void setUsedList(List<Block> newUsedList) { this.usedList = newUsedList; }
    public Block getNextBlock() { return nextBlock; }
    public void setNextBlock(Block newBlock) { this.nextBlock = newBlock; }

    /**
     * This method creates a pool with a size inputted by user.
     */
    public void pool(String algorithm, int size) {
        this.algorithm = algorithm;
        this.poolSize = size;
        freeList.add(new Block("pool", size, 0));
    }

    /**
     * Allocate a block with size {size} from the free list. This method works in first fit mode now.
     */
    public Block alloc(String blockName, int size) {
        Block newBlock = null;
        if (freeList.size() == 0) {
            throw new RuntimeException("There is no pool space yet. Please create the pool first.");
        } else {
            // Choose the block to be divided based on algorithm.
            Block blockChosen;
            if (this.algorithm.equals("first")) {
                blockChosen = firstFitAlloc(size);
            } else if (this.algorithm.equals("worst")) {
                blockChosen = worstFitAlloc(size);
            } else if (this.algorithm.equals("best")) {
                blockChosen = bestFitAlloc(size);
            } else if (this.algorithm.equals("random")) {
                blockChosen = randomFitAlloc(size);
            } else if (this.algorithm.equals("next")) {
                blockChosen = nextFitAlloc(size);
            } else {
                throw new RuntimeException("ALLOC ERROR: Algorithm not valid!");
            }

            // Divide the chosen block into two blocks, and add one of them to the usedList.
            if (blockChosen == null) {
                failedAlloc++;
                System.out.println("ALLOCATION FAILED: There is no available block that fits the size " + size + "\n");
            } else {
                newBlock = new Block(blockName, size, blockChosen.getOffset());
                blockChosen.setSize(blockChosen.getSize() - size);
                blockChosen.setOffset(blockChosen.getOffset() + size);
                usedList.add(newBlock);
                // Set nextBlock equal to blockChosen because blockChosen comes right after the last allocated block.
                nextBlock = blockChosen;
            }
        }
        return newBlock;
    }

    /**
     * This method performs the first fit algorithm,
     * and returns the chosen block that we should use to alloc memory.
     **/
    public Block firstFitAlloc(int size) {
        Block blockChosen = null;
        for (Block availableBlock : freeList) {
            if (availableBlock.getSize() >= size) {
                blockChosen = availableBlock;
                return blockChosen;
            }
        }
        return blockChosen;
    }

    /**
     * This method performs the worst fit algorithm,
     * and returns the chosen block that we should use to alloc memory.
     **/
    public Block worstFitAlloc(int size) {
        Block blockChosen = null;
        int maxSizeDifference = Integer.MIN_VALUE;
        for (Block availableBlock : freeList) {
            if ((availableBlock.getSize() - size) > maxSizeDifference) {
                maxSizeDifference = availableBlock.getSize() - size;
                blockChosen = availableBlock;
            }
        }
        return blockChosen;
    }

    /**
     * This method performs the best fit algorithm,
     * and returns the chosen block that we should use to alloc memory.
     **/
    public Block bestFitAlloc(int size) {
        Block blockChosen = null;
        int minSizeDifference = Integer.MAX_VALUE;
        for (Block availableBlock : freeList) {
            if (((availableBlock.getSize() - size) < minSizeDifference) &&
                    ((availableBlock.getSize() - size) > 0)) {
                minSizeDifference = availableBlock.getSize() - size;
                blockChosen = availableBlock;
            }
        }
        return blockChosen;
    }

    /**
     * This method performs the random fit algorithm,
     * and returns the chosen block that we should use to alloc memory.
     **/
    public Block randomFitAlloc(int size) {
        Random rand = new Random();
        int index = rand.nextInt(freeList.size());
        while (freeList.get(index).getSize() < size) {
            index = rand.nextInt(freeList.size());
        }
        return freeList.get(index);
    }

    /**
     * This method performs the next fit algorithm,
     * a modified version of first fit that starts at nextBlock rather than the first block.
     */
    public Block nextFitAlloc(int size) {
        Block blockChosen = null;
        // If no previous block has been allocated, then the starting index should be at 0.
        int startingIndex = 0;
        if (nextBlock != null) {
            startingIndex = freeList.indexOf(nextBlock);
        }

        for (int i = startingIndex; i < freeList.size(); i++) {
            if (freeList.get(i).getSize() >= size) {
                blockChosen = freeList.get(i);
                return blockChosen;
            }
        }
        return blockChosen;
    }

    /**
     * This method frees a block from the usedList and put it back to freeList.
     */
    public void free(String blockName) {
        Block blockToFree = null;
        // Find the block that corresponds to the given name.
        for (Block usedBlock : usedList) {
            if (usedBlock.getName().equals(blockName)) {
                blockToFree = usedBlock;
            }
        }

        // Throw an error if the block to free doesn't exit.
        if (blockToFree == null) {
            System.out.println("FREE ERROR: No such block in used list.");
            return;
        } else {
            usedList.remove(blockToFree);
            freeList.add(blockToFree);
            freeList = sortAndMerge(freeList);
        }
    }

    /**
     * This method takes an input list, sorts the list based on blocks' offset and merges adjacent blocks.
     */
    public List<Block> sortAndMerge(List<Block> listToMerge) {
        List<Block> mergedList = new ArrayList<>();
        if (listToMerge == null) { return null; }
        if (listToMerge.size() == 0) { return mergedList; }

        // Sort the input list.
        Collections.sort(listToMerge, new SortBlockByOffset());
        Block initialBlock = listToMerge.get(0);

        // Merge blocks if they are adjacent to each other.
        for (int i = 1; i < listToMerge.size(); i++) {
            if (initialBlock.isAdjacent(listToMerge.get(i))) {
                initialBlock.setSize(initialBlock.getSize() + listToMerge.get(i).getSize());
            } else {
                mergedList.add(initialBlock);
                initialBlock = listToMerge.get(i);
            }
        }
        mergedList.add(initialBlock);
        return mergedList;
    }

    /** This method calculates and prints out the percentage of used and free memory. It also prints the number of failed allocation. */
    public void usageCalculation(){
        int freeSpace = 0;
        for (Block freeBlock : freeList) {
            freeSpace += freeBlock.getSize();
        }

        int usedSpace = 0;
        for (Block usedBlock : usedList) {
            usedSpace += usedBlock.getSize();
        }

        assert(freeSpace+usedSpace == poolSize);
        System.out.println("Percentage of used memory: " + (float)usedSpace/poolSize +
                "; Percentage of free memory: " + (float)freeSpace/poolSize);
        System.out.println("Number of Failed Allocation: " + failedAlloc);
    }

    /**
     * This method is for testing whether the freeList and usedList both contain correct blocks.
     */
    public void printLists() {
        Collections.sort(freeList, new SortBlockByOffset());
        for (Block freeBlock : freeList) {
            System.out.println("free block: " + freeBlock.toString());
        }

        Collections.sort(usedList, new SortBlockByOffset());
        for (Block usedBlock : usedList) {
            System.out.println("used block: " + usedBlock.toString());
        }
    }

    public static void main(String[] args) {
        if ((args.length == 0) || (!(args[0] instanceof String))) {
            throw new RuntimeException("Please give an input file");
        }
        Simulation sim = new Simulation();

        // Read input from given file.
        List<String> inputs = new ArrayList<>();
        try {
            inputs = Files.readAllLines(new File(args[0]).toPath());
        } catch (IOException e) {
            System.out.println(e);
        }

        // Process each line of input and call corresponding method based on the first command.
        for (String command : inputs) {
            String[] commands = command.split(" ");
            // Check that each line of input has at least one word.
            assert (commands.length > 1);
            if (commands[0].equals("pool")) {
                assert (commands.length == 3);
                sim.pool(/* algorithm */commands[1], /* pool_size */Integer.valueOf(commands[2]));
            } else if (commands[0].equals("alloc")) {
                assert (commands.length == 3);
                sim.alloc(/* block_name */commands[1], /* block_size */Integer.valueOf(commands[2]));
            } else if (commands[0].equals("free")) {
                assert (commands.length == 2);
                sim.free(/* block name */commands[1]);
            }
        }
        sim.printLists();
        System.out.println();
        sim.usageCalculation();
    }
}
