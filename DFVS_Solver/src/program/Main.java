package program;

import gurobi.GRBException;
import program.algo.*;
import program.heuristics.Heuristics;
import program.ilp.ILPSolver;
import program.log.Log;
import program.model.*;
import program.utils.Dataset;
import program.utils.InstanceCreator;

import java.util.List;

public class Main {

    public static void main(String[] args) throws GRBException {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName), -1);

            // Solve
            ILPSolver.dfvsSolveInstance(instance);

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

            List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_2, null);
            Heuristics.testQuality(files);
        }
    }

    private static void testLowerBoundPerformance(List<GraphFile> files) {
        int count = 0;
        long millisAgg = 0;
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromFile(file, -1);
            if(instance.OPTIMAL_K == -1) continue;
            Graph graph = instance.subGraphs.get(0);
            List<Integer> reduceS = Reduction.applyRules(graph, true);
            int remainingK = instance.OPTIMAL_K - reduceS.size();
            if(remainingK <= 0) continue;
            // Compute packing
            long startTime = System.nanoTime();
            CyclePacking packing = new CyclePacking(graph.copy());
            int lowerBound = packing.size();
            long millis = (System.nanoTime() - startTime) / 1000000;

            Log.debugLog(instance.NAME, "Computed lower bound = " + lowerBound + " in " + millis + " ms");
            count++;
            millisAgg += millis;
        }
        Log.debugLog("Overall (" + count + ")", "Average millis: " + millisAgg / count);
    }

    private static void testLowerBoundQuality(List<GraphFile> files) {
        int lowerBoundAgg = 0;
        int remainingKAgg = 0;
        float qualityAgg = 0;
        int count = 0;
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromFile(file, -1);
            if(instance.OPTIMAL_K == -1) continue;
            Graph graph = instance.subGraphs.get(0);
            // Apply reduction rules and calculate remaining k for lower bounds
            List<Integer> reduceS = Reduction.applyRules(graph, true);
            int remainingK = instance.OPTIMAL_K - reduceS.size();

            CyclePacking packing = new CyclePacking(graph.copy());
            int lowerBound = packing.size();

            //PackingManager packing = new PackingManager(graph.copy());
            //int lowerBound = packing.size();

            if(remainingK > 0) {
                lowerBoundAgg += lowerBound;
                remainingKAgg += remainingK;
                qualityAgg += (float) lowerBound / remainingK;
                count++;
            }
            Log.debugLog(instance.NAME, "Lower bound: " + lowerBound + " / " + remainingK);
        }
        float quality = qualityAgg / count;
        Log.debugLog("Overall (" + count + ")", "Average lower bound quality per instance: " + quality);
        float overallLowerBoundK = (float) lowerBoundAgg / remainingKAgg;
        Log.debugLog("Overall (" + count + ")", "Overall lower bound quality: " + overallLowerBoundK);
    }
}
