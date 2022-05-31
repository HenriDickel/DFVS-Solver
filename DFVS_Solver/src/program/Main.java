package program;

import jscip.*;
import program.algo.*;
import program.ilp.ILPSolver;
import program.ilp.ILPSolverScip;
import program.log.Log;
import program.model.*;
import program.utils.*;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            System.loadLibrary("jscip");
            Scip scip = new Scip();
            scip.create("Example");

            // Start timer
            Timer.start(Integer.MAX_VALUE);

            // Create instance
            Instance instance = InstanceCreator.createPaceInstanceFromSystemIn();

            // Solve
            ILPSolver.solve(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            //testScipILP();
            //exportToGraphViz();

            //List<GraphFile> files = InstanceCreator.getPaceFilesExact(null);
            //files = files.subList()
        }
    }

    private static void testScipILP() {
        System.loadLibrary("jscip");
        // Solutions were calculated and verified by Gurobi and our custom solver
        int[] optimalKs = {2, 674, 631, 276, 450, 1917, 547, 788, 364, 1137, 510, 452, 273, 575, 715, 2079, 277, 2179, 2679, 400, 2509, 521, 2837, 2666, 2043, 2086, 2628, 1948, 1955, 214, 554, 2267, 3709, 513, 2092, 675, 4420, 4069, 5292, 4906, 4887, 745, 6254, 6253, 4928, 755, 10616, 5198, 9797, 4752, 21900, 10159, 58, 8888, 12710, 1741, 918, 24084, 24091, -1, 73, -1, 22898, 1469, -1, -1, -1, -1, 68, 38321, 40325, 40318, 1526, -1, 55, -1, -1, -1, -1, -1};
        List<GraphFile> files = InstanceCreator.getPaceFilesExact(null);
        files = files.subList(0, 50);

        List<GraphFile> solved = new ArrayList<>();
        List<GraphFile> unsolved = new ArrayList<>();
        List<Integer> solutions = new ArrayList<>();
        List<Long> runtimes = new ArrayList<>();
        for(int i = 0; i < files.size(); i++) {
            GraphFile file = files.get(i);
            Timer.start(900);
            PerformanceTimer.reset();

            PerformanceTimer.start();
            Instance instance = InstanceCreator.createFromPaceFile(file);
            ILPSolver.instance = instance;
            PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

            // Apply reduction rules once
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);

            // Create sub graphs
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Apply reduction rules again
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }

            int singleEdgeCount = 0;
            int overallEdgeCount = 0;
            try {
                for(Graph subGraph: instance.subGraphs) {

                    // Count edges in subgraph
                    for(Node node: subGraph.getNodes()) {
                        for(Integer outId: node.getOutIds()) {
                            if(!node.getInIds().contains(outId)) {
                                singleEdgeCount++;
                            }
                        }
                    }
                    overallEdgeCount += subGraph.getEdgeCount();

                    // Solve with ILP
                    //Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Call SCIP for subgraph with |N| = " + subGraph.getNodeCount() + ", |M| = " + subGraph.getEdgeCount());
                    List<Integer> S = ILPSolverScip.solve(subGraph.copy(), Timer.getSecondsLeft());
                    instance.S.addAll(S);
                }

                // Add nodes that were ambiguous to result
                instance.addAmbiguousResult();

                Color color = Color.WHITE;
                if(i < optimalKs.length) {
                    boolean verified = instance.S.size() == optimalKs[i];
                    color = verified ? Color.PURPLE : Color.RED;
                }
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Found solution k = " + instance.S.size() + " in " + Timer.getMillis() + " ms", color);
                solved.add(file);
                solutions.add(instance.S.size());
                runtimes.add(Timer.getMillis());
            } catch(TimeoutException e) {

                // Log results
                Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Found no solution in " + Timer.getMillis() + " ms", Color.RED);
                unsolved.add(file);
                solutions.add(-1);
                runtimes.add(Timer.getMillis());
            }
        }

        System.out.println("Solved " + solved.size() + "/" + files.size() + " instances");
        System.out.println("Unsolved: " + unsolved.stream().map(e -> e.name).collect(Collectors.toList()));

        for(int i = 0; i < solutions.size(); i++) {
            Color color = Color.WHITE;
            if(i < optimalKs.length) {
                boolean verified = solutions.get(i) == optimalKs[i];
                color = verified ? Color.PURPLE : Color.RED;
            }
            Log.debugLog(files.get(i).name + " (" + (i + 1) + ")", "k = " + solutions.get(i) + ", runtime = " + runtimes.get(i) + " ms", color);
        }
        System.out.println("Average runtime: " + runtimes.stream().mapToInt(Long::intValue).sum() / runtimes.size());
    }

    private static void exportToGraphViz(){
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_105");
        files = files.subList(0, 1);

        for(GraphFile file: files) {
            // Get instance
            Instance instance = InstanceCreator.createFromPaceFile(file);
            ILPSolver.instance = instance;

            // Reduce
            List<Integer> reduceInit = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceInit);

            // Create sub graphs
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Reduce again
            for(Graph graph : instance.subGraphs){
                List<Integer> reduce =  Reduction.applyRules(graph, true);
                instance.S.addAll(reduce);
            }
            GraphVizExport.ExportGraph(instance, "reduced");
        }
    }
}
