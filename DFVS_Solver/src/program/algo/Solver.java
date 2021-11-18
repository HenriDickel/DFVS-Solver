package program.algo;

import program.model.Cycle;
import program.model.Instance;
import program.utils.TimeoutException;
import program.model.Graph;
import program.model.Node;
import program.log.Log;
import program.utils.Timer;


import java.util.*;

public abstract class Solver {

    private static List<Node> dfvsBranch(Graph graph, int k, int level) throws TimeoutException {

        // Check Timer
        if(Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");


        // Break to skip the redundant dfvs_branch()-call when k = 0
        if(k <= 0) {
            // Return if graph has no circles
            if(DAG.isDAG(graph)) return new ArrayList<>();
            else return null;
        }

        // Next Cycle
        //Cycle cycle = ShortestCycle.run(graph);
        Cycle cycle = BFSShortestCircle.ShortestCircleBFS(graph);

        // Loop
        for(Node node: cycle.getNodes()){
            if(node.forbidden < level) continue;
            node.forbidden = level;
            node.delete();
            List<Node> S = dfvsBranch(graph, k - 1, level + 1);
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
        }

        // Reset forbidden (for nodes forbidden on this level)
        for(Node node: cycle.getNodes()) {
            if(node.forbidden == level) node.forbidden = Integer.MAX_VALUE;
        }
        return null;
    }

    public static List<Node> dfvsSolve(Graph graph){
        // Start k
        int m = graph.getEdgeCount();
        int n = graph.getActiveNodes().size();
        int k = MinMaxK.minK(m, n);

        // Solution
        List<Node> S = null;

        // Loop
        while(S == null){
            S = dfvsBranch(graph, k, 0);
            k++;
        }

        // Return solution
        return S;
    }

    public static void dfvsSolveInstance(Instance instance) {

        // Start Timer
        Timer.start();

        // Preprocessing
        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");
        Preprocessing.fullyRemoveSelfEdges(instance);
        Preprocessing.fullyRemovePairs(instance);
        Log.debugLog(instance.NAME, "Removed " + instance.solvedK + " nodes in preprocessing");

        // Create sub graphs
        Graph initialGraph = instance.subGraphs.get(0);
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.NAME, initialGraph);

        // Sort nodes in sub graphs
        Preprocessing.sortNodesByEdgeCount(instance);

        // Run for all sub graphs
        try{
            for(Graph subGraph: instance.subGraphs) {
                List<Node> S = dfvsSolve(subGraph);
                instance.S.addAll(S);
                instance.solvedK += S.size();
            }
        }
        catch(TimeoutException timeoutException){
            Long time = Timer.stop();
            Log.mainLog(instance, time, false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(time), true);
            return;
        }

        // Stop Timer
        Long time = Timer.stop();

        // Verify
        boolean verified = instance.solvedK == instance.OPTIMAL_K;

        // Log
        Log.mainLog(instance, time, verified);
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.solvedK + " in " + Timer.format(time), !verified);
    }

}
