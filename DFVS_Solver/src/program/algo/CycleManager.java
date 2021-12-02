package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;


public class CycleManager {

    private final Map<Node, Cycle> bestCycles = new LinkedHashMap<>();

    public CycleManager(Graph graph) {
        initShortestCycles(graph, graph.nodes);
    }

    public CycleManager(CycleManager cycleManager, Graph graph, Node deletedNode) {
        for(Map.Entry<Node, Cycle> entry: cycleManager.bestCycles.entrySet()) {
            Node node = entry.getKey();
            if(!node.deleted) {
                Cycle prevCycle = entry.getValue();
                if(prevCycle.contains(deletedNode)) {
                    Cycle shortestCycle = SimpleBFS.findBestCycle(graph, node, Integer.MAX_VALUE);
                    if(shortestCycle != null) bestCycles.put(node, shortestCycle);
                } else {
                    bestCycles.put(node, cycleManager.bestCycles.get(node));
                }
            }
        }
    }

    public Cycle getBestCycle() {
        Cycle bestCycle = null;
        for(Cycle cycle: bestCycles.values()) {
            if(bestCycle == null || cycle.getBranchSize() < bestCycle.getBranchSize()) bestCycle = cycle;
        }
        return bestCycle;
    }

    public void initShortestCycles(Graph graph, List<Node> nodes) {
        for(Node node: nodes) {
            if(!node.deleted) {
                Cycle shortestCycle = SimpleBFS.findBestCycle(graph, node, Integer.MAX_VALUE);
                if(shortestCycle != null) bestCycles.put(node, shortestCycle);
            }
        }
    }
}
