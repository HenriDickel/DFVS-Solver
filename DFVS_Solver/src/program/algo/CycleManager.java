package program.algo;


import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public class CycleManager {

    private final Map<Integer, Cycle> shortestCycles = new LinkedHashMap<>();
    int minSize = Integer.MAX_VALUE;

    public CycleManager(Graph graph) {
        initShortestCycles(graph);
    }

    public CycleManager(CycleManager prevCycleManager, Graph graph) {

        for(Node node: graph.getNodes()) {

            Cycle prevCycle = prevCycleManager.shortestCycles.get(node.id);

            // When there is a previous cycle, possibly keep it
            if (prevCycle != null) {
                boolean destroyed = false;
                Cycle copyCycle = new Cycle();
                for (Node cycleNode : prevCycle.getNodes()) {
                    if (!graph.hasNode(cycleNode.id)) {
                        destroyed = true;
                        break;
                    } else {
                        copyCycle.add(graph.getNode(cycleNode.id));
                    }
                }
                if (!destroyed) {
                    shortestCycles.put(node.id, copyCycle);
                    minSize = Math.min(minSize, copyCycle.size());
                }
            }
        }
    }

    public Cycle findShortestCycle(Graph graph){

        List<Cycle> cycles = graph.getPairCycles();

        // When there are no cycles of size 2, look for shortest cycles with BFS
        if(cycles.size() == 0) {
            for (Node node : graph.getNodes()) { // Find the best cycle for each node
                Cycle cycle = shortestCycles.get(node.id);
                if(cycle == null) {
                    cycle = SimpleBFS.findBestCycle(graph, node, minSize);
                    if (cycle != null) shortestCycles.put(node.id, cycle);
                }

                // Replace the min branch size when found better one
                if (cycle != null) {
                    cycles.add(cycle);
                    minSize = Math.min(cycle.size(), minSize);
                }
            }
        } else {
            minSize = 2;
        }

        // Filter out all cycles which are longer than the min branch size
        final int finalMinSize = minSize;
        List<Cycle> minCycles = cycles.stream().filter(cycle -> cycle.size() == finalMinSize).collect(Collectors.toList());

        // Set cycle count for every node
        for(Node node: graph.getNodes()) {
            node.cycleCount = 0;
            for(Cycle cycle: cycles) {
                if(cycle.contains(node)) {
                    node.cycleCount++;
                }
            }
        }

        // Find the cycle, whose nodes appear most in other cycles
        Cycle shortestCycle = null;
        int maxCycleCount = 0;
        for(Cycle cycle: minCycles) {
            int cycleCount = 0;
            for(Node node: cycle.getNodes()) {
                cycleCount += node.cycleCount;
            }
            if(cycleCount > maxCycleCount) { // if new max found, replace the current best cycle
                shortestCycle = cycle;
                maxCycleCount = cycleCount;
            }
        }

        // Throw exception, when no best cycle is found
        if(shortestCycle == null) {
            throw new RuntimeException("Full BFS didn't found a cycle!");
        }

        // Sort nodes in cycle by their frequency in other cycles
        shortestCycle.getNodes().sort(Comparator.comparing(Node::getCycleCount));
        Collections.reverse(shortestCycle.getNodes());
        return shortestCycle;
    }

    private void initShortestCycles(Graph graph) {
        for(Node node: graph.getNodes()) {
            Cycle cycle = SimpleBFS.findBestCycle(graph, node, minSize);
            if(cycle != null) {
                shortestCycles.put(node.id, cycle);
                minSize = Math.min(minSize, cycle.size());
            }
        }
    }
}
