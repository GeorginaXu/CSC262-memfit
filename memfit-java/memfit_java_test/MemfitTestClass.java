import edu.smith.cs.csc262.memfit.Simulation;
import org.junit.Assert;
import org.junit.Test;
import edu.smith.cs.csc262.memfit.Block;
import java.util.List;
import java.util.ArrayList;


public class MemfitTestClass {

    @Test
    public void testPool(){
        Simulation sim = new Simulation();
        sim.pool("algorithm", 1000);
        assert(sim.getAlgorithm().equals("algorithm"));
        assert(sim.getPoolSize() == 1000);
        assert(sim.getFreeList().size() == 1);
        assert(sim.getFreeList().get(0).getOffset() == 0);
    }

    @Test
    public void testFirstFit(){
        Simulation sim = new Simulation();
        // Manually create a freeList.
        List<Block>  freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock1", 500, 0));
        freeList.add(new Block("freeBlock2", 400, 600));
        sim.setFreeList(freeList);

        Block blockChosen = sim.firstFitAlloc(100);
        assert(blockChosen == freeList.get(0));
    }

    @Test
    public void testBestFit(){
        Simulation sim = new Simulation();
        // Manually create a freeList.
        List<Block>  freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock1", 500, 0));
        freeList.add(new Block("freeBlock2", 400, 600));
        freeList.add(new Block("freeBlock3", 200, 1200));
        sim.setFreeList(freeList);

        Block blockChosen = sim.bestFitAlloc(100);
        assert(blockChosen == freeList.get(2));
    }

    @Test
    public void testWorstFit(){
        Simulation sim = new Simulation();
        // Manually create a freeList.
        List<Block>  freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock1", 500, 0));
        freeList.add(new Block("freeBlock2", 400, 600));
        freeList.add(new Block("freeBlock3", 200, 1200));
        sim.setFreeList(freeList);

        Block blockChosen = sim.worstFitAlloc(100);
        assert(blockChosen == freeList.get(0));
    }

    @Test
    public void testNextFit(){
        Simulation sim = new Simulation();
        // Manually create a freeList.
        List<Block>  freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock1", 500, 0));
        freeList.add(new Block("freeBlock2", 200, 600));
        freeList.add(new Block("freeBlock3", 400, 1200));
        sim.setFreeList(freeList);

        // Test that next fit behaves just like first fit, when no block has been previously alloctaed.
        Block blockChosen = sim.nextFitAlloc(300);
        assert(blockChosen == freeList.get(0));

        // Test that next fit starts searching at the previously allocated block.
        sim.setNextBlock(freeList.get(1));
        Block blockChosen2 = sim.nextFitAlloc(300);
        assert(blockChosen2 == freeList.get(2));
    }


    @Test
    public void testAlloc(){
        Simulation sim = new Simulation();
        List<Block> freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock", 1000, 0));
        sim.setFreeList(freeList);
        sim.setAlgorithm("first");

        // Test if failedAlloc increases if allocation fails.
        Block allocatedBlock = sim.alloc("A", 2000);
        assert(allocatedBlock == null);
        assert(sim.getFailedAlloc() == 1);

        // Test if successful allocation behaves as intended.
        allocatedBlock = sim.alloc("A", 300);
        assert(allocatedBlock.getName().equals("A"));
        assert(allocatedBlock.getSize() == 300);
        assert(allocatedBlock.getOffset() == 0);

        assert(sim.getUsedList().size() == 1);
        assert(sim.getFreeList().get(0).getSize() == 700);
        assert(sim.getFreeList().get(0).getOffset() == 300);
        assert(sim.getNextBlock().getName().equals("freeBlock"));
        assert(sim.getNextBlock().getSize() == 700);
        assert(sim.getNextBlock().getOffset() == 300);
    }

    @Test
    public void testSortAndMerge(){
        Simulation sim = new Simulation();
        // Create a free list with no adjacent blocks.
        List<Block>  freeList1 = new ArrayList<>();
        freeList1.add(new Block("freeBlock1", 500, 300));
        freeList1.add(new Block("freeBlock2", 200, 0));
        freeList1.add(new Block("freeBlock3", 400, 1200));

        // If none of the blocks are adjacent to each other,
        // the resulting list should be the same size but in sorted order.
        List<Block> resultingList1 = sim.sortAndMerge(freeList1);
        assert(resultingList1.size() == 3);
        assert(resultingList1.get(0).getName().equals("freeBlock2"));
        assert(resultingList1.get(1).getName().equals("freeBlock1"));
        assert(resultingList1.get(2).getName().equals("freeBlock3"));

        // Create a free list with two adjacent blocks.
        List<Block>  freeList2 = new ArrayList<>();
        freeList2.add(new Block("freeBlock1", 500, 200));
        freeList2.add(new Block("freeBlock2", 200, 0));
        freeList2.add(new Block("freeBlock3", 400, 1200));

        // If two blocks are adjacent to each other,
        // the resulting list should have a smaller size.
        List<Block> resultingList2 = sim.sortAndMerge(freeList2);
        assert(resultingList2.size() == 2);
        assert(resultingList2.get(0).getName().equals("freeBlock2"));
        assert(resultingList2.get(1).getName().equals("freeBlock3"));

        // Create a free list with multiple adjacent blocks.
        List<Block>  freeList3 = new ArrayList<>();
        freeList3.add(new Block("freeBlock1", 500, 200));
        freeList3.add(new Block("freeBlock2", 200, 0));
        freeList3.add(new Block("freeBlock3", 400, 1200));
        freeList3.add(new Block("freeBlock4", 100, 700));
        freeList3.add(new Block("freeBlock5", 200, 1600));

        // free block 1, 2, 4 should be merged to one block; free block 3, 5 should be merged to one block.
        List<Block> resultingList3 = sim.sortAndMerge(freeList3);
        assert(resultingList3.size() == 2);
        assert(resultingList3.get(0).getName().equals("freeBlock2"));
        assert(resultingList3.get(1).getName().equals("freeBlock3"));

        // Test the method when giving in an empty list.
        List<Block> emptyList = new ArrayList<>();
        assert(sim.sortAndMerge(emptyList).size() == 0);

        // Test the method when giving in a null list.
        assert(sim.sortAndMerge(null) == null);
    }

    @Test
    public void testFree(){
        Simulation sim = new Simulation();
        // Create a free list with no adjacent blocks.
        List<Block>  freeList = new ArrayList<>();
        freeList.add(new Block("freeBlock1", 500, 300));
        freeList.add(new Block("freeBlock2", 200, 0));
        freeList.add(new Block("freeBlock3", 400, 1200));
        sim.setFreeList(freeList);

        List<Block>  usedList = new ArrayList<>();
        usedList.add(new Block("usedBlock1", 100, 1000));
        sim.setUsedList(usedList);

        // Test free method when no merging occurs.
        sim.free("usedBlock1");
        assert(sim.getUsedList().size() == 0);
        assert(sim.getFreeList().size() == 4);

        // Test free method when merging occurs (usedBlock2 will merge with freeBlock 3).
        usedList.add(new Block("usedBlock2", 200, 1600));
        sim.setUsedList(usedList);
        sim.free("usedBlock2");
        assert(sim.getUsedList().size() == 0);
        assert(sim.getFreeList().size() == 4);

        // Test that nothing changes when trying to free a not-existing used block.
        usedList.add(new Block("usedBlock3", 300, 1800));
        sim.setUsedList(usedList);
        sim.free("not-existing-block");
        assert(sim.getUsedList().size() == 1);
        assert(sim.getFreeList().size() == 4);
    }
}
