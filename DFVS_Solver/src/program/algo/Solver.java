package program.algo;

import program.log.CycleCounter;
import program.model.*;
import program.utils.TimeoutException;
import program.log.Log;
import program.utils.Timer;


import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    public static Instance instance;

    private static List<Node> dfvsBranch(Graph graph, int k, int level) throws TimeoutException {

        // Log recursive steps
        instance.recursiveSteps++;

        // Check Timer
        if (Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if (k <= 0) {
            // Return if graph has no circles
            if (DAG.isDAG(graph)) {
                return new ArrayList<>();
            }
            else return null;
        }

        // Next Cycle
        Cycle cycle = FullBFS.findShortestCycle(graph);

        // Log cycle
        CycleCounter.count(cycle, level);

        List<Integer> forbiddenIds = new ArrayList<>();
        for (Node node: cycle.getNodes()) {
            // Create a copy of the graph and remove deleted & forbidden nodes
            Graph copy = graph.copy();
            copy.removeNode(node);
            copy.removeForbiddenNodes(forbiddenIds);
            // Recursive call
            List<Node> S = dfvsBranch(copy, k - 1, level + 1);
            if (S != null) {
                S.add(node);
                return S;
            }
            forbiddenIds.add(node.id);
        }
        return null;
    }

    public static List<Node> dfvsSolve(Graph initialGraph) {

        // Set Petals
        Flowers.SetAllPetals(initialGraph);
        List<Node> flowers = new ArrayList<>();

        /*
        String petalsString = "{" + graph.getActiveNodes().stream().map(node -> String.valueOf(node.petal)).collect(Collectors.joining(",")) + "}";

         Use flower rule 1
        int removedByRule1Count = 0;
        for(Node node : graph.getActiveNodes()){
            if(node.petal == 1){
                for(Node out : node.getInNeighbors()){
                    for(Node in : node.getInNeighbors()){
                        in.addOutNeighbor(out);
                    }
                }
                graph.fullyRemoveNode(node);
                removedByRule1Count++;
            }
        }
        Log.debugLog(instance.NAME, "Petal Values: " + petalsString);
        Log.debugLog(instance.NAME, "Removed " + removedByRule1Count + " nodes by petal rule 1");
        */

        // Loop
        int k = 0;
        List<Node> S = null;
        while (S == null) { // Loop
            // copy graph (to remove nodes with flower rule)
            Graph copy = initialGraph.copy();

            // No need to recalculate flowers if there were none in previous step
            if (k == 0 || flowers.size() > 0) {
                // Use Petal Rule
                flowers = Flowers.UsePetalRule(copy, k);
            }

            // No need to use algorithm if we found too many flowers
            if (flowers.size() <= k) {
                int kBudget = k - flowers.size();
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
        Log.debugLog(instance.NAME, "Removed " + flowers.size() + " flower nodes by petal rule 2");
        instance.flowerDeletedNodes += flowers.size();
        S.addAll(flowers);
        return S;
    }

    public static void dfvsSolveInstance(Instance instance) {

        //Set instance & branch count
        Solver.instance = instance;

        // Start Timer
        Timer.start();

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");
        Preprocessing.applyRules(initialGraph);
        Preprocessing.removePendantFullTrianglePP(initialGraph);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Apply rules on each sub graph
        instance.subGraphs.forEach(Preprocessing::applyRules);
        instance.preDeletedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.solvedK;
        Log.debugLog(instance.NAME, "Removed " + instance.preDeletedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        // Run for all sub graphs
        try {
            for (Graph subGraph : instance.subGraphs) {
                List<Node> S = dfvsSolve(subGraph);
                instance.S.addAll(S);
                instance.solvedK += S.size();
            }
        } catch (TimeoutException timeoutException) {
            Long time = Timer.stop();
            Log.mainLog(instance, time, false);
            Log.detailLog(instance);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", true);
            return;
        }

        // Stop Timer
        Long time = Timer.stop();

        // Verify
        boolean verified = instance.solvedK == instance.OPTIMAL_K;

        // Log
        Log.mainLog(instance, time, verified);
        Log.detailLog(instance);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.solvedK + " in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", !verified);
    }

}
