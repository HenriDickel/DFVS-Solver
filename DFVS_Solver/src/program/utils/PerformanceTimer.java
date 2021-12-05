package program.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class PerformanceTimer {

    private static long startTime;

    private static long millisPreprocessing = 0;
    private static long millisFlowers = 0;
    private static long millisBFS = 0;
    private static long millisDAG = 0;
    private static long millisCopy = 0;
    private static long millisReduction = 0;

    public enum MethodType {
        PREPROCESSING,
        FLOWERS,
        BFS,
        DAG,
        COPY,
        REDUCTION
    }

    public static void start() {
        startTime = System.nanoTime();
    }

    public static void log(MethodType type) {
        long millis = System.nanoTime() - startTime;
        switch (type) {
            case PREPROCESSING:
                millisPreprocessing += millis;
                break;
            case FLOWERS:
                millisFlowers += millis;
                break;
            case BFS:
                millisBFS += millis;
                break;
            case DAG:
                millisDAG += millis;
                break;
            case COPY:
                millisCopy += millis;
                break;
            case REDUCTION:
                millisReduction += millis;
                break;
        }
    }

    public static void printResult() {
        System.out.println("# Preprocessing: " + millisPreprocessing / 1000000.0f + " ms");
        System.out.println("# Flowers: " + millisFlowers / 1000000.0f + " ms");
        System.out.println("# BFS: " + millisBFS / 1000000.0f + " ms");
        System.out.println("# DAG: " + millisDAG / 1000000.0f + " ms");
        System.out.println("# Copy: " + millisCopy / 1000000.0f + " ms");
        System.out.println("# Reduction: " + millisReduction / 1000000.0f + " ms");
    }

    public static void reset() {
        millisPreprocessing = 0;
        millisFlowers = 0;
        millisBFS = 0;
        millisDAG = 0;
        millisCopy = 0;
        millisReduction = 0;
    }
}
