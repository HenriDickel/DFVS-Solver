package program.algo;

import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.utils.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DFASHeuristicSolver {

    public static void solveInstance(Instance instance) {

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

        Graph initialGraph = instance.subGraphs.get(0);

        Graph arcGraph = new Graph();

        int runningId = 0;
        Map<Integer, Integer> plusIds = new HashMap<>();
        Map<Integer, Integer> minusIds = new HashMap<>();

        for(Node node: initialGraph.getNodes()) {
            int minusId = runningId;
            runningId++;
            int plusId = runningId;
            runningId++;
            minusIds.put(node.id, minusId);
            plusIds.put(node.id, plusId);
            arcGraph.addArc(minusId, plusId);
        }

        for(Node node: initialGraph.getNodes()) {
            int plusId = plusIds.get(node.id);
            for(Integer outId: node.getOutIds()) {
                int minusId = minusIds.get(outId);
                arcGraph.addArc(plusId, minusId);
            }
        }

        Log.debugLog(instance.NAME, "N: " + initialGraph.getNodeCount() + ", M: " + initialGraph.getEdgeCount());
        Log.debugLog(instance.NAME, "Arc graph N: " + arcGraph.getEdgeCount());

        while(!DAG.isDAGFast(arcGraph)) {
            Cycle cycle = FullBFS.findShortestCycle(arcGraph);
            int removeId = cycle.get(0).id;
            arcGraph.removeNode(removeId);
            instance.S.add(removeId);
        }
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size(), false);
    }
}
