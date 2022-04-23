package program;

import program.algo.*;
import program.heuristics.HeuristicSolver;
import program.heuristics.Heuristics;
import program.log.Log;
import program.model.*;
import program.packing.Packings;
import program.utils.*;
import program.utils.TimeoutException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
            HeuristicSolver.dfvsSolveInstance(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            //testCorrectness();
            //testReduction();

            //List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();
            //List<GraphFile> files = InstanceCreator.getUnsolvedFiles();
            //List<GraphFile> files = InstanceCreator.getHeuristicFiles(null);
            List<GraphFile> files = InstanceCreator.getPaceFiles(null);

            //files.forEach(Main::reductionExport);
            files.forEach(x -> run(x, true));
        }
    }

    private static void testCorrectness(){
        List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_2, null);
        files.forEach(x -> run(x, false));
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
            nodeCount += instance.getNodeCount();

            // Apply reduction rules once
            Log.debugLog(instance.NAME, "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);
            reduceNodeCount += instance.getNodeCount();

            // Create sub graphs
            Log.debugLog(instance.NAME, "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));
            tarjanNodeCount += instance.getNodeCount();

            // Apply reduction rules again
            Log.debugLog(instance.NAME, "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }
            secondReduceNodeCount += instance.getNodeCount();

            kAgg += instance.OPTIMAL_K;
            reduceSAgg += instance.S.size();
        }

        System.out.println("Average nodes: " + nodeCount / files.size() + " => " + reduceNodeCount / files.size() + " => " + tarjanNodeCount / files.size() + " => " + secondReduceNodeCount / files.size());
        System.out.println("Average k: " + kAgg / files.size() + ", reduced: " + reduceSAgg / files.size());
    }

    private static void reductionExport(GraphFile file){

        //Get instance
        Instance instance = InstanceCreator.createFromPaceFile(file);

        //Export original graph
        Export.ExportGraph(instance, "0_original");

        //Reduce
        List<Integer> reduceInit = Reduction.applyRules(instance.subGraphs.get(0), true);
        instance.S.addAll(reduceInit);
        Export.ExportGraph(instance, "1_first_reduced");

        //Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));
        Export.ExportGraph(instance, "2_tarjan");

        //Reduce again
        for(Graph graph : instance.subGraphs){
            List<Integer> reduce =  Reduction.applyRules(graph, true);
            instance.S.addAll(reduce);
        }
        Export.ExportGraph(instance, "3_second_reduced");

    }

    private static void run(GraphFile file, boolean isPaceData) {

        Timer.start(90);
        PerformanceTimer.reset();

        PerformanceTimer.start();
        Instance instance = isPaceData ? InstanceCreator.createFromPaceFile(file) : InstanceCreator.createFromFile(file);
        PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

        try {
            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);
            HeuristicSolver.dfvsSolveInstance(instance);

            // Verify
            instance.solvedK = instance.S.size();
            boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

            // Log results
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), verified);
            Color color = verified ? Color.WHITE : Color.RED;
            Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            PerformanceTimer.printResult(instance.NAME);

        } catch (TimeoutException e) {
            instance.solvedK = instance.S.size() + HeuristicSolver.currentK;

            // Log results
            Log.debugLogAdd("", true);
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            PerformanceTimer.printResult(instance.NAME);
        }
    }
}
