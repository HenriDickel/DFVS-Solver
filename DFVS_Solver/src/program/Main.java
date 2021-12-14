package program;

import program.algo.*;
import program.log.Log;
import program.model.*;
import program.utils.InstanceCreator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName));

            // Solve
            Solver.dfvsSolveInstance(instance);

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

            //List<GraphFile> files = InstanceCreator.getErrorFilesDataset2();
            //List<GraphFile> files = InstanceCreator.getSelectedFilesDataset3();

            List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles("synth-n_1000-m_107005-k_50-p_0.2.txt");
            for(GraphFile file: files) {
                Instance instance = InstanceCreator.createFromFile(file);
                Solver.dfvsSolveInstance(instance);
            }
        }
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
            Log.debugLog(instance.NAME, "Computed lower bound = " + lowerBound + " in " + millis + " ms");
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
            CyclePacking packing = new CyclePacking(graph.copy());
            Solver.instance = instance;
            Log.ignore = true;
            //packing.applyCostTPacking();
            Log.ignore = false;
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
