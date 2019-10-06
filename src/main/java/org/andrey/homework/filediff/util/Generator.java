package org.andrey.homework.filediff.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

public class Generator {
    /**
     * Creates a file with given number of randomly generated 32-bit integers (1 per line).
     *
     * @param path path to the file, which should be created
     * @param size number of integers to write to file
     * @throws IOException
     */
    public static void run(Path path, int size) throws IOException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            for (int i = 0; i < size; i++) {
                writer.write(random.nextInt() + System.lineSeparator());
            }
        }
    }

    /**
     * Method is used mainly for testing. Write given number of elements in range of [0 - mod)
     * @param path path to the file, which should be created
     * @param size number of integers to write to file
     * @param mod mod base
     * @throws IOException
     */
    public static void testRun(Path path, int size, int mod) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            for (int i = 0; i < size; i++) {
                writer.write(i % mod + System.lineSeparator());
            }
        }
    }
}
