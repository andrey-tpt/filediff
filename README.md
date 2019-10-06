The solution takes two files, and finds all the numbers (1 per line) that are present in both files.

### Design assumptions
There is a finite number of 32 bits integers. If we use standard java.util.Set, which can store all instances of 
java.lang.Integer (16 bytes per instance on 64 bit JVM),
in the worst case it will require the set to keep 64Gb in memory.

To overcome this limitation we split the files into partitions in such way that same integers
in every file go into the same partition (map phase). We come up with the mapping:
 `partition index -> list of smaller files in this partition for every original file`
  
After files are split it should be possible to perform in memory checking 
and collect the output for every partition (reduce phase). The checking itself
is performed via look ups in the set. 

### Complexity 
N - number of integers in every input file (does not have to be the same)
K - number of input files (2 in the example)
P - number of partitions

Memory usage (from reduce phase)
    
    O(16 *  N / P) ~ O(N/P) where: 
      16 - size of java.lang.Integer
      N / P - size of the partition per file
    
Time complexity
    
    1. Map phase - O(K * N) - iterate over all elements in all files and partition them, where
        
    2. Reduce phase - O(P * K * N / P ) - O(K * N) - because we partitioned the input files,
     the algorithm for every partition  is the same as if everything fits into memory. 
     Iterate over all files in every paritition to add element to a result set - O(1),
     check if given element exists (O(1)) and remove from result set if it doesn't - O(1).
  
### Dependencies
- Java 8
- Maven 3

### Build with Maven 
Clone the repository from git and make sure that all dependencies from above
are satisfied. Then run
```bash
     mvn clean package
```

### Running the code
Once artifact is prepared it can be executed with helper script from *bin*
directory of the project. 

```bash
    ./bin/run.sh
```

### Executing tests
Once artifact is preapred it should be possible to run tests from the project
```bash
    mvn test
``` 
Further improvements for the tests can be done (e.g. segregate Mapper and Reducer interfaces) 
and test them independently.