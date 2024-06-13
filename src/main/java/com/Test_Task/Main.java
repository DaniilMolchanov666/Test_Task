package com.Test_Task;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    public static Set<List<Long>> countOfNumbersFromFile = new LinkedHashSet<>();
    public static Set<Map<Long, Set<List<Long>>>> finalGroups = new LinkedHashSet<>();
    public static final String pathToFile = "./src/main/resources/file.txt";
    public static final String pathToOutputFile =  "./src/main/resources/output-file.txt";

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();

        Set<List<Long>> setOfValuesFromFile = getNumbersFromFile(pathToFile);
        if (!setOfValuesFromFile.isEmpty()) {
            int max = 0;
            for (List<Long> list : countOfNumbersFromFile) {
                if (max <= list.size())
                    max = list.size();
            }
            Set<Map<Long, Set<List<Long>>>> forTest = generateGroups(setOfValuesFromFile, max);
            print(forTest);
            System.out.println(getInfo());
            long end = System.currentTimeMillis();
            System.out.println("Общее время выполнения:" + (end - start)/1000 + " секунд");
        } else {
            System.out.println("Incorrect file!");
        }
    }

    public static boolean isValidString(String s) {
        return s.startsWith("\"") && s.endsWith("\"")
                && s.contains(";") && s.length() > 2;
    }

    public static Set<List<Long>> getNumbersFromFile(String pathToFile) throws FileNotFoundException {
        try {
            List<String> list = Files.readAllLines(Path.of(pathToFile));

            Set<List<Long>> set = list.stream().filter(Main::isValidString)
                    .map(s -> s.replace("\"\"", "0"))
                    .map(s -> StringUtils.remove(s, "\""))
                    .map(s -> Arrays.stream(s.split(";"))
                            .map(Long::valueOf)
                            .toList())
                    .collect(Collectors.toSet());
            countOfNumbersFromFile.addAll(set);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new LinkedHashSet<>(countOfNumbersFromFile);
    }

    public static Set<Map<Long, Set<List<Long>>>> generateGroups(Set<List<Long>> copyOfMainSet, int maxElements) {
        int size = 0;
        Set<Map<Long, Set<List<Long>>>> setOfColumns = new LinkedHashSet<>();
        while (maxElements >= size) {

            int finalSize = size;
            List<Long> listOfColumnNumbers = copyOfMainSet.stream()
                    .filter( i -> i.size() > finalSize)
                    .map(i -> i.get(finalSize))
                    .toList();

            Set<Long> setForAddingFromList = new HashSet<>();
            Set<Long> setForAddingCopies = new HashSet<>();

            for (long l : listOfColumnNumbers) {
                if (l != 0 && !setForAddingFromList.add(l)) {
                    setForAddingCopies.add(l);
                }
            }

            Map<Long, Set<List<Long>>> mapWithMatches = copyOfMainSet.stream()
                    .filter(s -> s.stream()
                            .skip(finalSize)
                            .limit(1)
                            .anyMatch(setForAddingCopies::contains))
                    .collect(Collectors.groupingBy(
                            s -> s.stream()
                                    .skip(finalSize)
                                    .limit(1)
                                    .findFirst().orElse(0L),
                            Collectors.toSet()
                    ));

            if (!mapWithMatches.isEmpty()) {
                finalGroups.add(mapWithMatches);
                setOfColumns.add(mapWithMatches);
            }
            size++;
        }
        return setOfColumns;
    }

    public static void print(Set<Map<Long, Set<List<Long>>>> setOfDuplicatesInMaps) throws IOException {
        ;
        Path outputPath = Path.of(pathToOutputFile);
        if (!Files.exists(outputPath)) {
            try {
                Files.createFile(outputPath);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile());
             PrintStream out = new PrintStream(fileOutputStream)) {
            out.println(getInfo());
            setOfDuplicatesInMaps.forEach(m -> {
                for (Map.Entry<Long, Set<List<Long>>> longs : m.entrySet()) {
                    out.println("Группа " + longs.getKey());
                    longs.getValue().forEach(out::println);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getInfo() {
        return "Общее кол-во чисел: %d\nОбщее кол-во групп: %d\n"
                .formatted(countOfNumbersFromFile.size(), countOfGroups());
    }

    private static int countOfGroups() {
        int quantityOfGroups = 0;
        for (Map<Long, Set<List<Long>>> group : finalGroups) {
            quantityOfGroups += group.keySet().size();
        }
        return quantityOfGroups;
    }
}