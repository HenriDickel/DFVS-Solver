package program.algo;

import program.log.CycleCounter;
import program.model.*;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.log.Log;
import program.utils.Timer;


import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    public static Instance instance;

    private static List<Integer> dfvsBranch(Graph graph, int k, int level) throws TimeoutException {

        // Log recursive steps
        instance.recursiveSteps++;

        // Check Timer
        if (Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");

        // Checks, if k is within the lower bounds
        PerformanceTimer.start();
        if(CyclePacking.checkLowerBounds(graph, k)) return null;
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if (k <= 0) {
            // Return if graph has no circles
            PerformanceTimer.start();
            boolean isDAG = DAG.isDAG(graph);
            PerformanceTimer.log(PerformanceTimer.MethodType.DAG);
            if (isDAG) {
                return new ArrayList<>();
            }
            else return null;
        }

        // Next Cycle
        PerformanceTimer.start();
        Cycle cycle = FullBFS.findShortestCycle(graph);
        PerformanceTimer.log(PerformanceTimer.MethodType.BFS);

        // Log cycle
        CycleCounter.count(cycle, level);

        List<Integer> forbiddenIds = new ArrayList<>();
        for (Node node: cycle.getNodes()) {
            // Create a copy of the graph and remove deleted & forbidden nodes
            PerformanceTimer.start();
            Graph copy = graph.copy();
            copy.removeNode(node.id);
            copy.removeForbiddenNodes(forbiddenIds);
            PerformanceTimer.log(PerformanceTimer.MethodType.COPY);
            PerformanceTimer.start();
            List<Integer> reduceS = Reduction.applyRules(copy, false);
            PerformanceTimer.log(PerformanceTimer.MethodType.REDUCTION);
            int nextK = k - 1 - reduceS.size();
            if(nextK < 0) continue;
            // Recursive call
            List<Integer> S = dfvsBranch(copy, nextK, level + 1);
            if (S != null) {
                S.add(node.id);
                S.addAll(reduceS);
                return S;
            }
            forbiddenIds.add(node.id);
        }
        return null;
    }

    public static List<Integer> dfvsSolve(Graph initialGraph) {

        // Set Petals
        PerformanceTimer.start();
        Flowers.SetAllPetals(initialGraph);
        List<Integer> removedFlowers = new ArrayList<>();
        PerformanceTimer.log(PerformanceTimer.MethodType.FLOWERS);

        /*
        String petalsString = "{" + initialGraph.getNodes().stream().map(node -> String.valueOf(node.petal)).collect(Collectors.joining(",")) + "}";

         //Use flower rule 1
        int removedByRule1Count = 0;
        for(Node node : initialGraph.getNodes()){
            if(node.petal == 1){
                for(Integer out : node.getOutIds()){
                    for(Integer in : node.getInIds()){
                        initialGraph.getNode(in).addOutId(out);
                    }
                }
                initialGraph.removeNode(node.id);
                removedByRule1Count++;
            }
        }
        Log.debugLog(instance.NAME, "Petal Values: " + petalsString);
        Log.debugLog(instance.NAME, "Removed " + removedByRule1Count + " nodes by petal rule 1");
        */

        // Loop
        int k = 0;

        List<Integer> S = null;
        while (S == null) { // Loop

            // copy graph (to remove nodes with flower rule)
            Graph copy = initialGraph.copy();

            // No need to recalculate flowers if there were none in previous step
            if (k == 0 || removedFlowers.size() > 0) {
                // Use Petal Rule
                removedFlowers = Flowers.UsePetalRule(copy, k);
            }

            // No need to use algorithm if we found too many flowers
            if (removedFlowers.size() <= k) {
                int kBudget = k - removedFlowers.size();
                CycleCounter.init(kBudget);

                S = dfvsBranch(copy, kBudget, 0);
                if (S == null) {
                    // Log detail logs
                    instance.averageCycleSize = CycleCounter.getAverageCycleSize();
                    instance.recursiveStepsPerK = CycleCounter.getRecursiveSteps();
                }
            }
            k++;
        }

        // Return solution
        Log.debugLog(instance.NAME, "Removed " + removedFlowers.size() + " flower nodes by petal rule 2");
        instance.removedFlowers += removedFlowers.size();
        S.addAll(removedFlowers);
        return S;
    }

    public static void dfvsSolveInstance(Instance instance) {

        //Set instance & branch count
        Solver.instance = instance;

        // Start Timer
        Timer.start();

        Graph initialGraph = instance.subGraphs.get(0);

        PerformanceTimer.reset();
        PerformanceTimer.start();

        // Preprocessing
        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Apply rules on each sub graph
        for(Graph subGraph: instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }
        PerformanceTimer.log(PerformanceTimer.MethodType.PREPROCESSING);

        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        // Run for all sub graphs
        try {
            for (Graph subGraph : instance.subGraphs) {
                List<Integer> S = dfvsSolve(subGraph);
                instance.S.addAll(S);
            }
        } catch (TimeoutException timeoutException) {
            Long time = Timer.stop();
            PerformanceTimer.printResult();
            Log.mainLog(instance, time, false);
            Log.detailLog(instance);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", true);
            return;
        }

        // Stop Timer
        Long time = Timer.stop();

        // Verify
        boolean verified = instance.S.size() == instance.OPTIMAL_K;

        // Log
        PerformanceTimer.printResult();
        Log.mainLog(instance, time, verified);
        Log.detailLog(instance);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", !verified);
    }
}
