package program.algo;

import program.log.CycleCounter;
import program.model.*;
import program.packing.PackingManager;
import program.utils.Timer;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.log.Log;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    public static Instance instance;
    public static int currentK;

    private static List<Integer> dfvsBranch(Graph graph, int k, int level, PackingManager pm) throws TimeoutException {

        if(Timer.isTimeout()) throw new TimeoutException();

        // Log recursive steps
        instance.recursiveSteps++;

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if (k <= 0) {
            // Return if graph has no circles
            PerformanceTimer.start();
            boolean isDAG = DAG.isDAGFast(graph);
            PerformanceTimer.log(PerformanceTimer.MethodType.DAG);
            if (isDAG) {
                return new ArrayList<>();
            }
            else return null;
        }

        // Next Cycle
        PerformanceTimer.start();
        Cycle cycle = FullBFS.findBestCycle(graph);
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

            // Apply reduction rules
            PerformanceTimer.start();
            List<Integer> reduceS = Reduction.applyRules(copy, false);
            PerformanceTimer.log(PerformanceTimer.MethodType.REDUCTION);

            // Calculate next k, skip if < 0
            int nextK = k - 1 - reduceS.size();
            if(nextK < 0) continue;

            // Add all new deleted nodes to list
            List<Integer> deleteIds = new ArrayList<>(reduceS);
            deleteIds.add(node.id);
            // Update packing manager
            PerformanceTimer.start();
            PackingManager newPm = new PackingManager(pm, deleteIds, forbiddenIds);
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);

            // When packing is larger than next k, skip & try upgrade packing
            if(newPm.size() > nextK) {
                PerformanceTimer.start();
                newPm.addDeletedNodes(deleteIds);
                newPm.removeForbiddenNodes(forbiddenIds);
                newPm.initPacking();
                PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
                if(newPm.size() > pm.size()) pm = newPm;
                // If updated packing is > k, immediately return
                if(pm.size() > k) return null;
                else continue;
            }

            // Recursive call
            List<Integer> S = dfvsBranch(copy, nextK, level + 1, newPm);
            if (S != null) {
                S.add(node.id);
                S.addAll(reduceS);
                return S;
            }

            // Try upgrade packing
            PerformanceTimer.start();
            newPm.addDeletedNodes(deleteIds);
            newPm.removeForbiddenNodes(forbiddenIds);
            newPm.initPacking();
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
            if(newPm.size() > pm.size()) pm = newPm;

            // Add new node to forbidden nodes
            forbiddenIds.add(node.id);
        }
        return null;
    }

    public static List<Integer> dfvsSolve(Graph initialGraph) {

        PerformanceTimer.start();
        PackingManager pm = new PackingManager(initialGraph);
        Log.debugLog(instance.NAME, "Initial cycle packing size: " + pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);

        currentK = 0;
        List<Integer> S = null;
        Log.debugLogNoBreak(instance.NAME, "Branching with k =");
        while (S == null) {
            if(currentK >= pm.size()) {
                CycleCounter.init(currentK);
                Log.debugLogAdd(" " + currentK, false);
                S = dfvsBranch(initialGraph, currentK, 0, pm);
                if (S == null) {
                    // Log detail logs
                    instance.averageCycleSize = CycleCounter.getAverageCycleSize();
                    instance.recursiveStepsPerK = CycleCounter.getRecursiveSteps();
                }
            }
            currentK++;
        }
        Log.debugLogAdd("", true);
        return S;
    }

    public static void dfvsSolveInstance(Instance instance) throws TimeoutException {

        //Set instance & branch count
        Solver.instance = instance;

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        PerformanceTimer.start();
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

        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);
        PerformanceTimer.log(PerformanceTimer.MethodType.PREPROCESSING);

        // Run for all sub graphs
        for (Graph subGraph : instance.subGraphs) {

            //Check if there is no cycle
            if(DAG.isDAGFast(subGraph)) continue;

            List<Integer> S = dfvsSolve(subGraph);
            instance.S.addAll(S);
        }
    }
}
