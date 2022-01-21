package program;

import gurobi.GRBException;
import program.algo.*;
import program.ilp.ILPSolver;
import program.log.Log;
import program.model.*;
import program.utils.InstanceCreator;
import program.utils.PerformanceTimer;
import program.utils.Timer;

import java.util.List;
import java.util.concurrent.*;

public class Main {

    private static final int TIME_OUT = 90;

    private static Instance instance;

    public static void main(String[] args) throws GRBException {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName));

            // Solve
            ILPSolver.dfvsSolveInstance(instance, TIME_OUT);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }

            // Print recursive steps
            System.out.println("#recursive steps: " + instance.recursiveSteps);
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles("synth-n_4000-m_1691611-k_150-p_0.2.txt");
            for(GraphFile file: files) {
                long startTime = System.currentTimeMillis();
                Instance instance = InstanceCreator.createFromFile(file);
                Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");
                ILPSolver.dfvsSolveInstance(instance, TIME_OUT);
                long millis = (System.currentTimeMillis() - startTime);
                if(millis > TIME_OUT * 1000) {
                    // Log solution
                    PerformanceTimer.printILPResult();
                    Log.ilpLog(instance, millis, false);
                    Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(millis), true);
                } else {
                    // Log solution
                    PerformanceTimer.printILPResult();
                    Log.ilpLog(instance, millis, true);
                    Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(millis), false);
                }
            }
        }
    }

    private static void run(GraphFile file) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(() -> {
            long startTime = System.currentTimeMillis();
            instance = InstanceCreator.createFromFile(file);
            Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");
            ILPSolver.dfvsSolveInstance(instance, TIME_OUT);
            return (System.currentTimeMillis() - startTime);
        });

        long millis = TIME_OUT * 1000;
        try {
            millis = future.get(TIME_OUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            future.cancel(true);
            executor.shutdownNow();

            // Log solution
            PerformanceTimer.printILPResult();
            Log.ilpLog(instance, millis, false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(millis), true);
            return;
        }
        executor.shutdownNow();

        // Log solution
        PerformanceTimer.printILPResult();
        Log.ilpLog(instance, millis, true);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(millis), false);
    }

    private static void testLowerBoundPerformance(List<GraphFile> files) {
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromFile(file);
            Graph graph = instance.subGraphs.get(0);
            List<Integer> reduceS = Reduction.applyRules(graph, true);
            // Compute packing
            long startTime = System.nanoTime();
            CyclePacking packing = new CyclePacking(graph.copy());
            int lowerBound = packing.size();
            long millis = (System.nanoTime() - startTime) / 1000000;

            // Compute slow packing
            long slowStartTime = System.nanoTime();
            SlowPacking slowPacking = new SlowPacking(graph.copy());
            int slowLowerBound = slowPacking.size();
            long slowMillis = (System.nanoTime() - slowStartTime) / 1000000;

            Log.debugLog(instance.NAME, "Computed lower bound = (" + lowerBound + " / " + slowLowerBound + ") in (" + millis + " / " + slowMillis + ") ms");
        }
    }

    private static void testLowerBoundQuality(List<GraphFile> files) {
        int lowerBoundAgg = 0;
        int remainingKAgg = 0;
        float qualityAgg = 0;
        int count = 0;
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromFile(file);
            Graph graph = instance.subGraphs.get(0);
            // Apply reduction rules and calculate remaining k for lower bounds
            List<Integer> reduceS = Reduction.applyRules(graph, true);
            int remainingK = instance.OPTIMAL_K - reduceS.size();

            //CyclePacking packing = new CyclePacking(graph.copy());
            //int lowerBound = packing.size();

            PackingManager packing = new PackingManager(graph.copy());
            int lowerBound = packing.size();

            if(remainingK > 0) {
                lowerBoundAgg += lowerBound;
                remainingKAgg += remainingK;
                qualityAgg += (float) lowerBound / remainingK;
                count++;
            }
            Log.debugLog(instance.NAME, "Lower bound: " + lowerBound + " / " + remainingK);
        }
        float quality = qualityAgg / count;
        Log.debugLog("Overall", "Average lower bound quality per instance: " + quality);
        float overallLowerBoundK = (float) lowerBoundAgg / remainingKAgg;
        Log.debugLog("Overall", "Overall lower bound quality: " + overallLowerBoundK);
    }
}
