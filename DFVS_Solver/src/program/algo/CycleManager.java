package program.algo;


import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public class CycleManager {

    private final Map<Integer, Cycle> shortestCycles = new LinkedHashMap<>();
    private int minSize = Integer.MAX_VALUE;

    public CycleManager(Graph graph) {
        initShortestCycles(graph);
    }

    public CycleManager(CycleManager prevCycleManager, Graph graph) {

        for(Node node: graph.getNodes()) {

            Cycle prevCycle = prevCycleManager.shortestCycles.get(node.id);

            // When there is a previous cycle, possibly keep it
            if (prevCycle != null) {
                boolean destroyed = false;
                for (Node cycleNode : prevCycle.getNodes()) {
                    if (!graph.hasNode(cycleNode.id)) {
                        destroyed = true;
                        break;
                    }
                }
                if (!destroyed) {
                    shortestCycles.put(node.id, prevCycle);
                    minSize = Math.min(minSize, prevCycle.size());
                    continue;
                }
            }

            Cycle cycle = SimpleBFS.findBestCycle(graph, node, minSize);
            if (cycle != null) {
                shortestCycles.put(node.id, cycle);
                minSize = Math.min(minSize, cycle.size());
            }
        }
    }

    public Cycle getShortestCycle() {

        List<Cycle> cycles = shortestCycles.values().stream().toList();
        // Filter out all cycles which are longer than the min branch size
        List<Cycle> minCycles = cycles.stream().filter(cycle -> cycle.size() == minSize).collect(Collectors.toList());

        // Find the cycle, whose nodes appear most in other cycles
        Cycle shortestCycle = null;
        int maxCycleCount = 0;
        for(Cycle cycle: minCycles) {
            int cycleCount = 0;
            for(Node node: cycle.getNodes()) {
                node.cycleCount = 0;
                for(Cycle otherCycle: cycles) {
                    if(otherCycle.contains(node)) {
                        node.cycleCount++;
                        cycleCount++;
                    }
                }
            }
            if(cycleCount > maxCycleCount) { // if new max found, replace the current best cycle
                shortestCycle = cycle;
                maxCycleCount = cycleCount;
            }
        }

        // Throw exception, when no best cycle is found
        if(shortestCycle == null) {
            throw new RuntimeException("BFS didn't found a cycle!");
        }

        // Sort nodes in cycle by their frequency in other cycles
        shortestCycle.getNodes().sort(Comparator.comparing(Node::getCycleCount));
        Collections.reverse(shortestCycle.getNodes());
        return shortestCycle;

    }

    public void initShortestCycles(Graph graph) {
        for(Node node: graph.getNodes()) {
            Cycle cycle = SimpleBFS.findBestCycle(graph, node, Integer.MAX_VALUE);
            if(cycle != null) {
                shortestCycles.put(node.id, cycle);
                minSize = Math.min(minSize, cycle.size());
            }

        }
    }
}
