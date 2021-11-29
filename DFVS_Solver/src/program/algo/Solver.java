package program.algo;

import program.log.CycleCounter;
import program.model.Cycle;
import program.model.Instance;
import program.utils.TimeoutException;
import program.model.Graph;
import program.model.Node;
import program.log.Log;
import program.utils.Timer;


import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    public static Instance instance;

    private static List<Node> dfvsBranch(Graph graph, int k, int level, CycleManager cycleManager) throws TimeoutException {

        instance.recursiveSteps++;

        // Check Timer
        if (Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");


        // Break to skip the redundant dfvs_branch()-call when k = 0
        if (k <= 0) {
            // Return if graph has no circles
            if (DAG.isDAG(graph)) return new ArrayList<>();
            else return null;
        }

        // Next Cycle
        Cycle cycle = cycleManager.getShortestCycle();
        // Log cycle
        CycleCounter.count(cycle, level);

        // Loop
        for (Node node : cycle.getNodes()) {
            if (node.forbidden < level) continue;
            node.forbidden = level;
            node.delete();
            List<Node> S = dfvsBranch(graph, k - 1, level + 1, new CycleManager(cycleManager, graph, node));
            node.unDelete();
            if (S != null) {
                S.add(node);
                return S;
            }
        }

        // Reset forbidden (for nodes forbidden on this level)
        for (Node node : cycle.getNodes()) {
            if (node.forbidden == level) node.forbidden = Integer.MAX_VALUE;
        }
        return null;
    }

    public static List<Node> dfvsSolve(Graph graph) {

        // Set Petals
        Flower.SetAllPetals(graph);
        List<Node> flowers = new ArrayList<>();

        // Loop
        int k = 0;
        List<Node> S = null;
        while (S == null) { // Loop

            // No need to recalculate flowers if there were none in previous step
            if (k == 0 || flowers.size() > 0) {
                //Use Petal Rule
                flowers = Flower.UsePetalRule(graph, k);
            }

            // No need to use algorithm if we found too many flowers
            if (flowers.size() <= k) {
                int kBudget = k - flowers.size();
                CycleCounter.init(kBudget);
                S = dfvsBranch(graph, kBudget, 0, new CycleManager(graph));
                if (S == null) {
                    // Log detail logs
                    instance.averageCycleSize = CycleCounter.getAverageCycleSize();
                    instance.averageBranchSize = CycleCounter.getAverageBranchSize();
                    instance.recursiveStepsPerK = CycleCounter.getRecursiveSteps();
                }
            }
            // Reset Petal values to initial values and undelete all nodes
            Flower.ResetPetalValues(graph);
            k++;
        }

        // Return solution
        Log.debugLog(instance.NAME, "Removed " + flowers.size() + " flower nodes by petal rule");
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
        Preprocessing.removePendantFullTrianglePP(instance);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(g -> g.nodes.size()).collect(Collectors.toList()));

        // Apply rules on each sub graph
        instance.subGraphs.forEach(Preprocessing::applyRules);
        instance.preDeletedNodes = instance.N - instance.subGraphs.stream().mapToInt(subGraph -> subGraph.nodes.size()).sum();
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
