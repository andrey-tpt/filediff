package org.andrey.homework.filediff;

import org.andrey.homework.filediff.util.Generator;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MapReduceTest {
    private Path basePath;

    @Before
    public void setUp() throws IOException {
        basePath = Files.createTempDirectory("test");
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(basePath.toFile());
    }

    @Test
    public void shouldProduceExpectedNumbers() throws IOException {
        Path path1 = Paths.get(basePath.toString(), "1");
        Path path2 = Paths.get(basePath.toString(), "2");
        Path pathOut = Paths.get(basePath.toString(), "out");
        int mod = 10;

        Generator.testRun(path1, 100, mod + 10); // should have values from 0 to 19 repeated
        Generator.testRun(path2, 50, mod); //should have values from 0 to 9 repeated
        //common is [0-10)

        MapReduce mr = new MapReduce(basePath, 4);
        mr.map(Arrays.asList(path1, path2));
        mr.reduce(pathOut);

        //Now check that there's only 0 is in the file

        Scanner sc = new Scanner(pathOut);
        Set<Integer> resultSet = IntStream.range(0, mod).mapToObj(i -> Integer.parseInt(sc.nextLine())).
                collect(Collectors.toSet());

        Assert.assertFalse(sc.hasNextLine()); // no more lines in the file

        Assert.assertEquals(mod, resultSet.size());
        IntStream.range(0, mod).forEach(i -> Assert.assertTrue(resultSet.contains(i)));
    }
}
