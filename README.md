# CSC262 memfit

_Name: Georgina Xu_

### Memory Allocation Assignment (Due Monday, 4 March 2019)

Test Input: 

```
pool best 2000
alloc A 100
alloc B 200
free A
alloc C 300
alloc D 50
free B
alloc E 3000
```

The results should be look like:

```
free list: 
      A (offset 50, size 250)   --->      originalPool(offset 600, size 1400)

used list:
      D (offset 0, size 50)     --->      C (offset 300, size 300)

```

The input file is stored under the CSC262-memfit folder as "input.txt." To run my Simulation class, please give in "input.txt" as an argument. The program will print out all the allocation failures and free error when it occurs. It will also print out the free list and used list at the very end. 

The unit test class "MemfitTestClass" is under the memfit_java_test directory. Running the class should be simple and straightforward with no extra steps needed.
