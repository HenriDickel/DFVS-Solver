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
    private static long millisPackingDAG = 0;
    private static long millisPackingBFS = 0;

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
        PACKING_DAG,
        PACKING_BFS
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
            case PACKING_DAG:
                millisPackingDAG += millis;
                break;
            case PACKING_BFS:
                millisPackingBFS += millis;
                break;
        }
        return millis;
    }

    public static long getPackingMillis() {
        return millisPacking + millisPackingBFS + millisPackingDAG;
    }

    public static void printResult() {
        Log.debugLog(Solver.instance.NAME, "BFS: " + millisBFS + ", Packing: " + millisPacking + ", Packing DAG: " + millisPackingDAG + ", Packing BFS: "
                + millisPackingBFS + ", Copy: " + millisCopy + ", Red: " + millisReduction + ", DAG: " + millisDAG + ", Pre: " + millisPreprocessing + ", File: " + millisFile, Color.YELLOW);
    }


    public static void printILPResult() {
        Log.debugLog(Solver.instance.NAME, "Preprocessing: " + millisPreprocessing / 1000 + "s, BFS: " + millisBFS / 1000 + " ms, Packing: " + millisPacking / 1000 + "s, ILP: " + millisILP / 1000 + "s");
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
        millisPackingDAG = 0;
        millisPackingBFS = 0;
    }
}
