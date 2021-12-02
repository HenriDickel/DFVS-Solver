package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FullBFS {

    public static Cycle findBestCycle(Graph g){


        int minBranchSize = Integer.MAX_VALUE;
        List<Cycle> cycles = new ArrayList<>();
        for (Node node : g.getActiveNodes()) { // Find the best cycle for each node

            Cycle cycle = SimpleBFS.findBestCycle(g, node, minBranchSize);

            // Replace the min branch size when found better one
            if (cycle != null) {
                cycles.add(cycle);
                minBranchSize = Math.min(cycle.getBranchSize(), minBranchSize);
            }
        }

        // Filter out all cycles which are longer than the min branch size
        final int finalMinBranchSize = minBranchSize;
        List<Cycle> minCycles = cycles.stream().filter(cycle -> cycle.getBranchSize() == finalMinBranchSize).collect(Collectors.toList());

        // Find the cycle, whose nodes appear most in other cycles
        Cycle bestCycle = null;
        int maxOtherCycles = 0;
        for(Cycle cycle: minCycles) {
            int otherCycles = 0;
            for(Node node: cycle.getNodes()) {
                node.otherCycles = 0;
                for(Cycle otherCycle: cycles) {
                    if(otherCycle.contains(node)) {
                        node.otherCycles++;
                        otherCycles++;
                    }
                }
            }
            if(otherCycles > maxOtherCycles) { // if new max found, replace the current best cycle
                bestCycle = cycle;
                maxOtherCycles = otherCycles;
            }
        }

        // Throw exception, when no best cycle is found
        if(bestCycle == null) {
            throw new RuntimeException("Full BFS didn't found a cycle!");
        }

        // Sort nodes in cycle by their frequency in other cycles
        bestCycle.getNodes().sort(Comparator.comparing(Node::getOtherCycles));
        Collections.reverse(bestCycle.getNodes());
        return bestCycle;
    }
}
