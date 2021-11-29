package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;


public class CycleManager {

    private final Map<Node, Cycle> shortestCycles = new LinkedHashMap<>();

    public CycleManager(Graph graph) {
        initShortestCycles(graph, graph.nodes);
    }

    public CycleManager(CycleManager cycleManager, Graph graph, Node deletedNode) {
        for(Map.Entry<Node, Cycle> entry: cycleManager.shortestCycles.entrySet()) {
            Node node = entry.getKey();
            if(!node.deleted) {
                Cycle prevCycle = entry.getValue();
                if(prevCycle.contains(deletedNode)) {
                    Cycle shortestCycle = SimpleBFS.findBestCycle(graph, node);
                    if(shortestCycle != null) shortestCycles.put(node, shortestCycle);
                } else {
                    shortestCycles.put(node, cycleManager.shortestCycles.get(node));
                }
            }
        }
    }

    public Cycle getShortestCycle() {
        Cycle shortestCycle = null;
        for(Cycle cycle: shortestCycles.values()) {
            if(shortestCycle == null || cycle.unforbiddenSize() < shortestCycle.unforbiddenSize()) shortestCycle = cycle;
        }
        return shortestCycle;
    }

    public void initShortestCycles(Graph graph, List<Node> nodes) {
        for(Node node: nodes) {
            if(!node.deleted) {
                Cycle shortestCycle = SimpleBFS.findBestCycle(graph, node);
                if(shortestCycle != null) shortestCycles.put(node, shortestCycle);
            }
        }
    }
}
