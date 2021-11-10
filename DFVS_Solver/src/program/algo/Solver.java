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

    private static List<Node> dfvsBranch(Graph graph, int k) throws TimeoutException {

        //Check Timer
        if(program.utils.Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");

        //Return if graph has no circles
        if(DAG.isDAG(graph)) return new ArrayList<>();

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if(k <= 0) return null;

        //Next Circle
        Cycle cycle = ShortestCycle.run(graph);

        //Loop
        for(Node node : cycle.getNodes()){
            node.delete();
            List<Node> S = dfvsBranch(graph, k - 1);
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
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
            S = dfvsBranch(graph, k);
            k++;
        }

        // Return solution
        return S;
    }

    public static void dfvsSolveInstance(Instance instance) {

        // Start Timer
        Timer.start();

        // Fully remove nodes with self edges
        Preprocessing.fullyRemoveSelfEdges(instance);

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
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(time), false);
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
