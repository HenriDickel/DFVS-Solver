package program;

import program.algo.*;
import program.heuristics.Solver;
import program.log.Log;
import program.model.*;
import program.utils.*;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Start timer
            Timer.start(Integer.MAX_VALUE);

            // Create instance
            Instance instance = InstanceCreator.createPaceInstanceFromSystemIn();

            // Solve
            Solver.dfvsSolveInstance(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            testPaceDataLite();
            //testPaceData();
            //testCorrectness();
            //exportPaceData();

            //testCorrectness();
            //testReduction();
            //paceReduction();

            //List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();
            //List<GraphFile> files = InstanceCreator.getUnsolvedFiles();
            //List<GraphFile> files = InstanceCreator.getHeuristicFiles(null);
            //List<GraphFile> files = InstanceCreator.getPaceFiles(null);

            //files.forEach(Main::reductionExport);
            //files.forEach(x -> run(x, true));
        }
    }

    private static void testPaceDataLite() {
        List<GraphFile> files = InstanceCreator.getPaceFiles(null);
        int[] optimalKs = new int[]{2, 674, 631, 276, 450, 1917, 547, 788, 364, 1137};

        for(int i = 0; i < 10; i++) {

            Timer.start(5);

            Instance instance = InstanceCreator.createFromPaceFile(files.get(i));

            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);

            Log.ignore = true;
            try {
                Solver.dfvsSolveInstance(instance);
                Log.ignore = false;

                Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

                // Verify
                instance.solvedK = instance.S.size();
                boolean verified = instance.solvedK == optimalKs[i];

                // Log results
                Color color = verified ? Color.WHITE : Color.RED;
                Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            }
            catch (TimeoutException e) {
                Log.ignore = false;
                Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            }
        }
    }

    private static void testPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFiles("e_085");
        files = files.subList(0, 1);
        files.forEach(x -> run(x, true));
    }

    private static void exportPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFiles("e_087");
        files = files.subList(0, 1);
        files.forEach(Main::reductionExport);
    }

    private static void testCorrectness(){
        List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
        files.forEach(x -> run(x, false));
    }

    private static void paceReduction() {
        List<GraphFile> files = InstanceCreator.getPaceFiles("e_003");
        files = files.subList(0, 1);

        for(GraphFile file: files) {

            Instance instance = InstanceCreator.createFromPaceFile(file);

            // Apply reduction rules once
            Log.debugLog(instance.NAME, "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);

            // Create sub graphs
            Log.debugLog(instance.NAME, "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Apply reduction rules again
            Log.debugLog(instance.NAME, "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }

            Log.paceLog(instance, 0L, instance.getCurrentN(), instance.getCurrentM(), instance.S.size(), 0, 0);
        }
    }

    private static void testReduction() {
        List<GraphFile> files = InstanceCreator.getPaceFiles(null);
        files = files.subList(0, 50);

        int nodeCount = 0;
        int reduceNodeCount = 0;
        int tarjanNodeCount = 0;
        int secondReduceNodeCount = 0;
        int kAgg = 0;
        int reduceSAgg = 0;
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromPaceFile(file);
            nodeCount += instance.getCurrentN();

            // Apply reduction rules once
            Log.debugLog(instance.NAME, "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);
            reduceNodeCount += instance.getCurrentN();

            // Create sub graphs
            Log.debugLog(instance.NAME, "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));
            tarjanNodeCount += instance.getCurrentN();

            // Apply reduction rules again
            Log.debugLog(instance.NAME, "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }
            secondReduceNodeCount += instance.getCurrentN();

            kAgg += instance.OPTIMAL_K;
            reduceSAgg += instance.S.size();
        }

        System.out.println("Average nodes: " + nodeCount / files.size() + " => " + reduceNodeCount / files.size() + " => " + tarjanNodeCount / files.size() + " => " + secondReduceNodeCount / files.size());
        System.out.println("Average k: " + kAgg / files.size() + ", reduced: " + reduceSAgg / files.size());
    }

    private static void reductionExport(GraphFile file){

        //Get instance
        Instance instance = InstanceCreator.createFromPaceFile(file);
        Solver.instance = instance;

        //Export original graph
        //Export.ExportGraph(instance, "original");

        //Reduce
        List<Integer> reduceInit = Reduction.applyRules(instance.subGraphs.get(0), true);
        instance.S.addAll(reduceInit);

        //Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

        //Reduce again
        for(Graph graph : instance.subGraphs){
            List<Integer> reduce =  Reduction.applyRules(graph, true);
            instance.S.addAll(reduce);
        }
        Export.ExportGraph(instance, "reduced");
    }

    private static void run(GraphFile file, boolean isPaceData) {

        Timer.start(90);
        PerformanceTimer.reset();

        PerformanceTimer.start();
        Instance instance = isPaceData ? InstanceCreator.createFromPaceFile(file) : InstanceCreator.createFromFile(file);
        PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

        try {
            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);
            Solver.dfvsSolveInstance(instance);

            // Verify
            instance.solvedK = instance.S.size();
            boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

            // Log results
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), verified);
            Color color = verified ? Color.WHITE : Color.RED;
            Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            PerformanceTimer.printResult(instance.NAME);

        } catch (TimeoutException e) {
            instance.solvedK = instance.S.size() + Solver.currentK;

            // Log results
            Log.debugLogAdd("", true);
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            PerformanceTimer.printResult(instance.NAME);
        }
    }
}
