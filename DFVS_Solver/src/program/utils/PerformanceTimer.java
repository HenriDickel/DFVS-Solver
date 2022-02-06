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
    private static long millisFile = 0;
    private static long millisHeuristic = 0;

    public enum MethodType {
        PREPROCESSING,
        FLOWERS,
        BFS,
        DAG,
        COPY,
        REDUCTION,
        PACKING,
        ILP,
        FILE,
        HEURISTIC
    }

    public static void start() {
        startTime = System.currentTimeMillis();
    }

    public static long log(MethodType type) {
        long millis = System.currentTimeMillis() - startTime;
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
            case FILE:
                millisFile += millis;
                break;
            case HEURISTIC:
                millisHeuristic += millis;
                break;
        }
        return millis;
    }

    public static long getPackingMillis() {
        return millisPacking;
    }

    public static void printResult(String name) {
        Log.debugLog(name, "BFS: " + millisBFS + ", Packing: " + millisPacking + ", Heuristic: " + millisHeuristic + ", Copy: " + millisCopy
                + ", Red: " + millisReduction + ", DAG: " + millisDAG + ", Pre: " + millisPreprocessing + ", File: " + millisFile, Color.YELLOW);

    }


    public static void printILPResult(String name) {
        Log.debugLog(name, "Preprocessing: " + millisPreprocessing / 1000 + "s, BFS: " + millisBFS / 1000 + " ms, Packing: " + millisPacking / 1000 + "s, ILP: " + millisILP / 1000 + "s");
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
        millisFile = 0;
        millisHeuristic = 0;
    }
}
