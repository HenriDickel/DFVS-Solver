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

            //testPaceData();
            //testCorrectness();
            exportPaceData();


            //List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();
            //List<GraphFile> files = InstanceCreator.getUnsolvedFiles();
            //List<GraphFile> files = InstanceCreator.getHeuristicFiles(null);

            //List<GraphFile> files = InstanceCreator.getPaceFiles(null);
            //files.forEach(Main::reductionExport);
            //files.forEach(Main::run);
        }
    }

    private static void testPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFiles(null);
        files.forEach(x -> run(x, true));
    }

    private static void exportPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFiles(null);
        files.forEach(Main::reductionExport);
    }

    private static void testCorrectness(){
        List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
        files.forEach(x -> run(x, false));
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

        //
        //List<String> ids = new ArrayList<>();
        //int preN = instance.getCurrentN();
        //for(Graph graph : instance.subGraphs){
        //    for(Node node : graph.getNodes()){
        //        if(Reduction.supersetRemove(graph, node)){
        //            ids.add(node.id.toString());
        //            graph.removeNode(node.id);
        //        }
        //    }
        //}
        //int postN = instance.getCurrentN();
        //Export.ExportGraph(instance, "4_single_edge_removed");
        //Collections.sort(ids);
        //System.out.println(String.join(", ", ids));
        //System.out.println(instance.NAME + ": Pre: " + preN + "\tPost: " + postN + "\t\tDiff: " + (preN - postN));
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
