package program.utils;

import program.algo.Solver;
import program.log.Log;

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
    private static long millisPacking = 0;

    public enum MethodType {
        PREPROCESSING,
        FLOWERS,
        BFS,
        DAG,
        COPY,
        REDUCTION,
        PACKING
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
            case PACKING:
                millisPacking += millis;
                break;
        }
    }

    public static void printResult() {
        Log.debugLog(Solver.instance.NAME, "Preprocessing: " + millisPreprocessing / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "Flowers: " + millisFlowers / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "BFS: " + millisBFS / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "DAG: " + millisDAG / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "Copy: " + millisCopy / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "Reduction: " + millisReduction / 1000000 + " ms");
        Log.debugLog(Solver.instance.NAME, "Packing: " + millisPacking / 1000000 + " ms");
    }

    public static void reset() {
        millisPreprocessing = 0;
        millisFlowers = 0;
        millisBFS = 0;
        millisDAG = 0;
        millisCopy = 0;
        millisReduction = 0;
        millisPacking = 0;
    }
}
