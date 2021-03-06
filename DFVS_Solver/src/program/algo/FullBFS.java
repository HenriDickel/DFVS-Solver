package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FullBFS {

    public static Cycle findShortestCycle(Graph graph){

        List<Cycle> cycles = graph.getPairCycles(); // TODO in rare cases (e.g. 'email'), it can be beneficial to break after the first cycle is found
        int minSize = 2;

        // When there are no cycles of size 2, look for shortest cycles with BFS
        if(cycles.size() == 0) {
            minSize = Integer.MAX_VALUE;
            for (Node node : graph.getNodes()) { // Find the best cycle for each node
                Cycle cycle = SimpleBFS.findBestCycle(graph, node, minSize);

                // Replace the min branch size when found better one
                if (cycle != null) {
                    cycles.add(cycle);
                    minSize = Math.min(cycle.size(), minSize);
                }
            }
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
}
