package org.andrey.homework.filediff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class MapReduce {
    /* java.lang.Integer is 16 Byte, there 2^32 integers possible so we can store all of them in 64 Gigabytes of memory)
       Size for set are minimum.
     */
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    private final int partitions;
    private final Path partitionsBasePath;
    private final Map<Integer, List<Path>> partitionsMap;

    public MapReduce(Path partitionsBasePath, int partitions) {
        if (null == partitionsBasePath || !Files.exists(partitionsBasePath))
            throw new IllegalArgumentException("partitionsBasePath must point to existing path");
        if (partitions <= 0)
            throw new IllegalArgumentException("Number of partitiions must be positive");

        this.partitions = partitions;
        this.partitionsBasePath = partitionsBasePath;
        this.partitionsMap = new HashMap<>();
    }

    public void map(List<Path> paths) throws IOException {
        for (int i = 0; i < paths.size(); i++) {
            mapFile(paths.get(i), i);
        }
    }

    private void mapFile(Path path, int index) throws IOException {
        List<Path> fileNames = createPartitionPaths(index);

        List<BufferedWriter> partitionWriters = null;
        try {
            partitionWriters = createPartitionWriters(fileNames);

            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int value = Integer.parseInt(line);
                    int writePartition = Math.abs(value) % partitions;
                    Writer writer = partitionWriters.get(writePartition);
                    writer.write(value + System.lineSeparator());
                }
            }
        } finally {
            if (partitionWriters != null) {
                partitionWriters.forEach(writer -> {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to close one the partitioned writers", e);
                    }
                });
            }
        }
    }

    private List<Path> createPartitionPaths(int fileIndex) {
        return IntStream.range(0, partitions).
                mapToObj(i -> {
                    Path p = Paths.get(partitionsBasePath.toString(), "_" + fileIndex + "_" + i);
                    partitionsMap.computeIfAbsent(i, k -> new ArrayList<>()).add(p);
                    return p;
                }).collect(toList());
    }

    private List<BufferedWriter> createPartitionWriters(List<Path> paths) {
        return paths.stream().map(p -> {
            try {
                return new BufferedWriter(new FileWriter(p.toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to create partition file: " + p, e);
            }
        }).collect(toList());
    }

    public void reduce(Path outputPath) throws IOException {
        for (int i = 0; i < partitions; i++) {
            List<Path> partitionedFilePaths = partitionsMap.get(i);
            Set<Integer> accumulator = reduce(partitionedFilePaths);
            output(outputPath, accumulator);
        }
    }

    private Set<Integer> reduce(List<Path> paths) throws IOException {
        Set<Integer> set = null; // can use something more optimal for primitives (e.g. GNU Trove),
        // but don't want to bring any other libraries (e.g. Trove)
        for (Path path : paths) {
            if (set == null) set = readPartition(path);
            else {
                set.retainAll(readPartition(path));
            }
        }

        return set;
    }

    private Set<Integer> readPartition(Path path) throws IOException {
        Set<Integer> set = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                set.add(Integer.parseInt(line));
            }
        }

        return set;
    }

    private void output(Path outputPath, Set<Integer> accumulator) throws IOException {
        //append to the output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile(), true))) {
            accumulator.forEach(x -> {
                try {
                    writer.write(x + System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
