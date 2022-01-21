package program.utils;

import program.algo.Solver;
import program.log.Log;

public abstract class PerformanceTimer {

    private static long startTime;

    private static long millisPreprocessing = 0;
    private static long millisFlowers = 0;
    private static long millisBFS = 0;
    private static long millisDAG = 0;
    private static long millisCopy = 0;
    private static long millisReduction = 0;
    private static long millisPacking = 0;
    private static long millisILP = 0;

    public enum MethodType {
        PREPROCESSING,
        FLOWERS,
        BFS,
        DAG,
        COPY,
        REDUCTION,
        PACKING,
        ILP
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
            case ILP:
                millisILP += millis;
                break;
        }
        startTime = System.nanoTime();
    }

    public static long getPackingMillis() {
        return millisPacking / 1000000;
    }

    public static void printResult() {
        Log.debugLog(Solver.instance.NAME, "Preprocessing: " + millisPreprocessing / 1000000 + " ms, Flowers: " + millisFlowers / 1000000 + " ms, BFS: " + millisBFS / 1000000 +
        " ms, DAG: " + millisDAG / 1000000 + " ms, Copy: " + millisCopy / 1000000 + " ms, Reduction: " + millisReduction / 1000000 + " ms, Packing: " + millisPacking / 1000000 + " ms");
    }

    public static void printILPResult() {
        Log.debugLog(Solver.instance.NAME, "Preprocessing: " + millisPreprocessing / 1000000 + " ms, BFS: " + millisBFS / 1000000 + " ms, Packing: " + millisPacking / 1000000 + " ms, ILP: " + millisILP / 1000000 + " ms");
    }

    public static void reset() {
        millisPreprocessing = 0;
        millisFlowers = 0;
        millisBFS = 0;
        millisDAG = 0;
        millisCopy = 0;
        millisReduction = 0;
        millisPacking = 0;
        millisILP = 0;
        startTime = System.nanoTime();
    }
}
