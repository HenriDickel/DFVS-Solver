package program.algo;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBVar;
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

    private static List<Integer> dfvsBranch(Graph graph, int k, int level, PackingManager pm) throws TimeoutException {

        // Log recursive steps
        instance.recursiveSteps++;

        // Check Timer
        if (Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " seconds.");

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
            //System.out.println(" . ".repeat(level) + "(" + node + ") PM packing size = " + newPm.size());
            //System.out.println("Packing time: " + (System.nanoTime() - PerformanceTimer.startTime) / 1000000);
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

    private static int k;

    public static List<Integer> dfvsSolve(Graph initialGraph) {

        PerformanceTimer.start();
        PackingManager pm = new PackingManager(initialGraph);
        Log.debugLog(instance.NAME, "PM: Initial cycle packing has the size " + pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);

        k = 0;
        List<Integer> S = null;
        while (S == null) {
            if(k >= pm.size()) {
                CycleCounter.init(k);
                Log.debugLog(instance.NAME, "Branching with k = " + k + " (+ " + instance.S.size() + ")...");
                S = dfvsBranch(initialGraph, k, 0, pm);
                if (S == null) {
                    // Log detail logs
                    instance.averageCycleSize = CycleCounter.getAverageCycleSize();
                    instance.recursiveStepsPerK = CycleCounter.getRecursiveSteps();
                }
            }
            k++;
        }
        return S;
    }

    public static void dfvsSolveInstanceILP(Instance instance) {

        //Set instance & branch count
        Solver.instance = instance;

        // Start Timer
        Timer.start();

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

        Graph initialGraph = instance.subGraphs.get(0);

        List<Integer> S = ILPSolverOrdering.solveGraph(initialGraph);
        instance.S.addAll(S);

        // Stop Timer
        Long millis = Timer.stop();

        // Log
        instance.solvedK = instance.S.size();
        //int status = model.get(GRB.IntAttr.Status);
        //boolean verified = status == GRB.Status.OPTIMAL;
        boolean verified = millis < Timer.timeout * 1000;
        Log.ilpLog(instance, millis, 0, verified);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(millis), !verified);
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

                //Check if there is no cycle
                if(DAG.isDAG(subGraph)) continue;

                //TODO
                //List<Integer> S = dfvsSolve(subGraph);
                List<Integer> S = ILPSolverOrdering.solveGraph(subGraph);
                //List<Integer> S = ILPSolverLazyCycles.solveGraph(subGraph);
                instance.S.addAll(S);
            }
        } catch (TimeoutException timeoutException) {
            Long time = Timer.stop();
            // Add the current k to the solution size for better logging
            instance.solvedK = instance.S.size() + k;
            PerformanceTimer.printResult();
            Log.mainLog(instance, time, PerformanceTimer.getPackingMillis(), false);
            Log.detailLog(instance);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", true);
            return;
        }

        // Stop Timer
        Long time = Timer.stop();

        // Verify
        instance.solvedK = instance.S.size();
        boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

        // Log
        PerformanceTimer.printResult();
        Log.mainLog(instance, time, PerformanceTimer.getPackingMillis(), verified);
        Log.detailLog(instance);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(time) + " (recursive steps: " + instance.recursiveSteps + ")", !verified);
    }
}
