package program.heuristics;

import program.algo.*;
import program.log.CycleCounter;
import program.log.Log;
import program.model.*;
import program.packing.PackingManager;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.*;
import java.util.stream.Collectors;

public abstract class HeuristicSolver {

    public static Instance instance;
    public static int currentK;

    public static void dfvsSolveInstance(Instance instance) throws TimeoutException {

        //Set instance & branch count
        HeuristicSolver.instance = instance;

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

            float nodePercentage = (float) subGraph.getNodeCount() / initialGraph.getNodeCount();
            List<Integer> S = dfvsSolveIncremental(subGraph, nodePercentage);
            instance.S.addAll(S);
        }
    }

    public static List<Integer> dfvsSolveIncremental(Graph graph, float nodePercentage) {

        // Calculate cycle packing
        PerformanceTimer.start();
        long packingTimeLimit = (long) (10000L * nodePercentage);
        Log.debugLog(instance.NAME, "Creating cycle packing with time limit = " + packingTimeLimit);
        PackingManager pm = new PackingManager(graph, packingTimeLimit);
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        instance.packingSize += pm.size();

        // Calculate heuristic
        PerformanceTimer.start();
        long heuristicTimeLimit = (long) (1000L * nodePercentage);
        Log.debugLog(instance.NAME, "Creating heuristic with time limit = " + heuristicTimeLimit);
        List<Integer> heuristicS = GraphTimerFast(graph, heuristicTimeLimit, 0.95f, pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.HEURISTIC);
        Log.debugLog(instance.NAME, "Solution lies in [" + pm.size() + ", " + heuristicS.size() + "]");

        // Initialize values
        float startPercentage = 0.5f;
        int kRange = heuristicS.size() - pm.size() - 1;
        currentK = pm.size() + Math.round(startPercentage * kRange);

        List<Integer> S;
        if(pm.size() == heuristicS.size()) {
            return heuristicS;
        } else {
            Log.debugLogNoBreak(instance.NAME, "Branching with k = " + currentK);
            S = dfvsBranch(graph, currentK, 0, pm);
        }

        if(S == null) { // move upwards
            while(S == null) {
                currentK++;
                Log.debugLogAdd(" " + currentK, false);
                if(currentK == heuristicS.size()) {
                    S = heuristicS;
                } else {
                    S = dfvsBranch(graph, currentK, 0, pm);
                }
            }
        } else { // move downwards
            List<Integer> lowerS = S;
            while(lowerS != null && currentK > pm.size()) {
                currentK--;
                Log.debugLogAdd(" " + currentK, false);
                lowerS = dfvsBranch(graph, currentK, 0, pm);
                if(lowerS != null) S = lowerS;
            }
        }
        Log.debugLogAdd("", true);
        return S;
    }

    public static List<Integer> dfvsSolveBinary(Graph graph, float nodePercentage) {

        // Calculate cycle packing
        PerformanceTimer.start();
        long packingTimeLimit = (long) (10000L * nodePercentage);
        Log.debugLog(instance.NAME, "Creating cycle packing with time limit = " + packingTimeLimit);
        PackingManager pm = new PackingManager(graph, packingTimeLimit);
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        instance.packingSize += pm.size();

        // Calculate heuristic
        PerformanceTimer.start();
        long heuristicTimeLimit = (long) (1000L * nodePercentage);
        Log.debugLog(instance.NAME, "Creating heuristic with time limit = " + heuristicTimeLimit);
        List<Integer> heuristicS = GraphTimerFast(graph, heuristicTimeLimit, 0.95f, pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.HEURISTIC);
        Log.debugLog(instance.NAME, "Solution lies in [" + pm.size() + ", " + heuristicS.size() + "]");


        int lowerBound = pm.size();
        int upperBound = heuristicS.size();
        List<Integer> S;
        if(pm.size()==upperBound){
            return heuristicS;
        }else{
            S = dfvsBranch(graph, upperBound-1, 0, pm);
            if(S==null){
                return heuristicS;
            }else{
                upperBound-=1;
                currentK = (lowerBound+upperBound)/2;
                S = null;
                List<Integer> lastFoundS = heuristicS;
                Log.debugLogNoBreak(instance.NAME, "Branching with k =");
                while (S == null) {
                    Log.debugLogAdd(" " + currentK, false);
                    S = dfvsBranch(graph, currentK, 0, pm);
                    if (S == null) {
                        lowerBound= currentK;
                    }else{
                        lastFoundS=S;
                        S = null;
                        upperBound = currentK;
                    }
                    if(upperBound==lowerBound){
                        return lastFoundS;
                    }
                    if(upperBound-lowerBound==1){
                        List<Integer> lowerBoundS = dfvsBranch(graph, lowerBound, 0, pm);
                        if(lowerBoundS == null){
                            return lastFoundS;
                        }else{
                            return lowerBoundS;
                        }
                    }
                    currentK= (lowerBound+upperBound)/2;
                }
            }
            Log.debugLogAdd("", true);
            return S;
        }

    }

    private static List<Integer> dfvsBranch(Graph graph, int k, int level, PackingManager pm) throws TimeoutException {

        if(Timer.isTimeout()) throw new TimeoutException();

        // Log recursive steps
        instance.recursiveSteps++;

        // Return if graph has no circles
        PerformanceTimer.start();
        boolean isDAG = DAG.isDAGFast(graph);
        PerformanceTimer.log(PerformanceTimer.MethodType.DAG);
        if (isDAG) {
            return new ArrayList<>();
        }

        // Next Cycle
        PerformanceTimer.start();
        Cycle cycle = FullBFS.findBestCycle(graph);
        PerformanceTimer.log(PerformanceTimer.MethodType.BFS);

        List<Integer> forbiddenIds = new ArrayList<>();
        for (Node node: cycle.getNodes()) {

            if(Timer.isTimeout()) throw new TimeoutException();
            //System.out.println("remove node " + node);
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
                newPm.updatePacking();
                PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
                if(newPm.size() > pm.size()) pm = newPm;
                // If updated packing is > k, immediately return
                if(pm.size() > k) {
                    return null;
                }
                else {
                    continue;
                }
            }

            // Recursive call
            //System.out.println("branch on level " + level);
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
            newPm.updatePacking();
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
            if(newPm.size() > pm.size()) pm = newPm;

            // Add new node to forbidden nodes
            forbiddenIds.add(node.id);
        }
        return null;
    }

    private static List<Integer> GraphTimerFast(Graph subGraph, long timeLimit, float precision, int lowerBound) {

        long endMillis = System.currentTimeMillis() + timeLimit;

        List<Integer> bestS = null;
        while(System.currentTimeMillis() <= endMillis){
            List<Integer> S = GraphTimerRecFast(subGraph, new ArrayList<>(), precision);

            if(S.size() == lowerBound) return S;
            if(bestS == null || S.size() < bestS.size()) {
                bestS = S;
            }

        }
        return bestS;
    }

    private static List<Integer> GraphTimerRecFast(Graph graph, List<Integer> solution, float precision) {

        //Check if DAG
        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> cycles = FullBFS.findMultipleShortestCycles(graph, false);

        //Copy
        Graph copyGraph = graph.copy();
        List<Integer> copySolution = new ArrayList<>(solution);

        //Random
        Random random = new Random();

        //Random
        for(int i = 0; i <= (float) cycles.size() * (1f - precision); i++){

            //Random cycle
            Cycle randomCycle = cycles.get(random.nextInt(cycles.size()));

            //Get best node
            Node node = Collections.max(randomCycle.getNodes(), Comparator.comparing(Node::getMinInOut));

            //Don't delete twice
            if(copySolution.contains(node.id)) continue;

            //Remove node
            copyGraph.removeNode(node.id);
            copySolution.add(node.id);

            // Remove destroyed cycles
            List<Cycle> remove = new ArrayList<>();
            for(Cycle cycle: cycles) {
                if(cycle.contains(node)) {
                    remove.add(cycle);
                }
            }
            for (Cycle cycle : remove) {
                cycles.remove(cycle);
            }

            //Reduction
            copySolution.addAll(Reduction.applyRules(copyGraph, false));

            if(cycles.isEmpty()) break;
        }

        //Do again
        return GraphTimerRecFast(copyGraph, copySolution, precision);

    }
}
