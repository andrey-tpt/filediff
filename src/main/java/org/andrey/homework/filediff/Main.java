package org.andrey.homework.filediff;

import org.andrey.homework.filediff.util.Generator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    private static int NUM_FILES = 2;
    private static int SIZE = 1024 * 1024;
    // Number of partitions is used to split generated input files into smaller files, which can fit into memory.
    private static int PARTITIONS = 4;

    public static void main(String[] args) throws IOException {
        Path basePath = Files.createTempDirectory("filediff");
        LOGGER.info("Created base directory: {}", basePath);

        Path outputPath = Paths.get(basePath.toString(), "out.txt");
        LOGGER.info("Output file: {}", outputPath);

        List<Path> inputFiles = IntStream.range(0, NUM_FILES).
                mapToObj(i -> Paths.get(basePath.toString(), i + ".txt")).
                collect(Collectors.toList());

        inputFiles.forEach(f -> {
            try {
                //Even though size is the same for all input files in this example it's not a requirement,
                // different values for different sizes can be provided.
                Generator.run(f, SIZE);
                LOGGER.info("Generated input file: {}", f);
            } catch (IOException e) {
                throw new RuntimeException("Unable to generate input file", e);
            }
        });

        MapReduce mr = new MapReduce(basePath, PARTITIONS);
        try {
            mr.map(inputFiles);
            LOGGER.info("Mapped input files into {} partitions", PARTITIONS);
            mr.reduce(outputPath);
            LOGGER.info("Reduced and combined the output into output file: {}", outputPath);

            peekOutput(outputPath);
        } catch (IOException e) {
            LOGGER.error("Unable to compare files", e);
        }
    }

    private static void peekOutput(Path outputFile) throws IOException {
        Scanner input = new Scanner(outputFile.toFile());
        int count = 0;
        int limit = 10;
        LOGGER.info("{} (or less) records from output file", limit);
        while (input.hasNextLine() && count < limit) {
            LOGGER.info("{}.{}", count, input.nextLine());
            count++;
        }
    }
}
