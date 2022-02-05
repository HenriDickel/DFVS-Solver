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

    public static void solveInstance(Instance instance) {

        HeuristicSolver.instance = instance;
        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Apply rules on each sub graph
        for (Graph subGraph : instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }

        // Log preprocessing
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        float totalNodeCount = instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        for (Graph subGraph : instance.subGraphs) {

            //Check if there is no cycle
            if(DAG.isDAGFast(subGraph)) continue;

            List<Integer> heuristicSol = GraphTimerFast(subGraph, 100, 0.95f, totalNodeCount);
            int upperBound = heuristicSol.size();
            List<Integer> S = dfvsHeuristicSolveIncremental(subGraph,heuristicSol,upperBound);
            //List<Integer> S = dfvsHeuristicSolveBinary(subGraph,heuristicSol,upperBound);

            instance.S.addAll(S);
        }
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size());

    }

    public static List<Integer> dfvsHeuristicSolveIncremental(Graph initialGraph, List<Integer> heuristicSol, int upperBound) {

        PerformanceTimer.start();
        PackingManager pm = new PackingManager(initialGraph);
        Log.debugLog(instance.NAME, "Initial cycle packing size: " + pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        List<Integer> S = null;
        if (pm.size() == upperBound) {
            return heuristicSol;
        } else {
            currentK = upperBound;
            List<Integer> lastFoundS = heuristicSol;
            Log.debugLogNoBreak(instance.NAME, "Branching with k =");
            do {
                currentK-=1;
                CycleCounter.init(currentK);
                Log.debugLogAdd(" " + currentK, false);
                S = dfvsHeuristicBranch(initialGraph, currentK, 0,pm);
                if(S==null){
                    return lastFoundS;
                }else{
                    lastFoundS=S;
                }
            } while (S != null);
        }
        Log.debugLogAdd("", true);
        return S;
    }

    public static List<Integer> dfvsHeuristicSolveBinary(Graph initialGraph, List<Integer> heuristicSol, int upperBound) {

        PerformanceTimer.start();
        PackingManager pm = new PackingManager(initialGraph);
        Log.debugLog(instance.NAME, "Initial cycle packing size: " + pm.size());
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        int lowerBound = pm.size();
        List<Integer> S;
        if(pm.size()==upperBound){
            return heuristicSol;
        }else{
            S = dfvsHeuristicBranch(initialGraph, upperBound-1, 0, pm);
            if(S==null){
                return heuristicSol;
            }else{
                upperBound-=1;
                currentK = (lowerBound+upperBound)/2;
                S = null;
                List<Integer> lastFoundS = heuristicSol;
                Log.debugLogNoBreak(instance.NAME, "Branching with k =");
                while (S == null) {
                    CycleCounter.init(currentK);
                    Log.debugLogAdd(" " + currentK, false);
                    S = dfvsHeuristicBranch(initialGraph, currentK, 0, pm);
                    if (S == null) {
                        // Log detail logs
                        instance.averageCycleSize = CycleCounter.getAverageCycleSize();
                        instance.recursiveStepsPerK = CycleCounter.getRecursiveSteps();
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
                        List<Integer> lowerBoundS = dfvsHeuristicBranch(initialGraph, lowerBound, 0, pm);
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

    private static List<Integer> dfvsHeuristicBranch(Graph graph, int k, int level, PackingManager pm) throws TimeoutException {

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
        //CycleCounter.count(cycle, level);

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
                newPm.fillPacking();
                PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
                if(newPm.size() > pm.size()) pm = newPm;
                // If updated packing is > k, immediately return
                if(pm.size() > k) return null;
                else continue;
            }

            // Recursive call
            List<Integer> S = dfvsHeuristicBranch(copy, nextK, level + 1, newPm);
            if (S != null) {
                S.add(node.id);
                S.addAll(reduceS);
                return S;
            }

            // Try upgrade packing
            PerformanceTimer.start();
            newPm.addDeletedNodes(deleteIds);
            newPm.removeForbiddenNodes(forbiddenIds);
            newPm.fillPacking();
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
            if(newPm.size() > pm.size()) pm = newPm;

            // Add new node to forbidden nodes
            forbiddenIds.add(node.id);
        }
        return null;
    }

    private static List<Integer> GraphTimerFast(Graph subGraph, int timeLimitMillis, float precision, float totalNodeCount) {

        // Destroy cycles by heuristic
        float percentage = (float) subGraph.getNodeCount() / totalNodeCount;
        long millisForThisGraph = (long) (timeLimitMillis * percentage);

        //All solutions
        List<List<Integer>> solutions = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        while(System.currentTimeMillis() <= startTime + millisForThisGraph){
            solutions.add(GraphTimerRecFast(subGraph, new ArrayList<>(), precision));
        }

        //Best solution
        List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
        return sol;
    }

    private static List<Integer> GraphTimerRecFast(Graph graph, List<Integer> solution, float precision) {

        //Check if DAG
        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, false);

        //Copy
        Graph copyGraph = graph.copy();
        List<Integer> copySolution = new ArrayList<>(solution);

        //Random
        Random random = new Random();

        //Random
        for(int i = 0; i <= (float) allShortestCycles.size() * (1f - precision); i++){

            //Random cycle
            Cycle randomCycle = allShortestCycles.get(random.nextInt(allShortestCycles.size()));

            //Get best node
            Node node = Collections.max(randomCycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

            //Don't delete twice
            if(copySolution.contains(node.id)) continue;

            //Remove node
            copyGraph.removeNode(node.id);
            copySolution.add(node.id);

            //Reduction
            copySolution.addAll(Reduction.applyRules(copyGraph, false));

            //Check if DAG
            if(DAG.isDAGFast(graph)) return solution;
        }

        //Do again
        return GraphTimerRecFast(copyGraph, copySolution, precision);

    }
}
