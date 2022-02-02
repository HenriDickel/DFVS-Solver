package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FullBFS {

    public static Cycle findBestCycle(Graph graph) {

        int maxMinInOut = 0;
        List<Node> maxMinInOutNodes = new ArrayList<>();
        for(Node node: graph.getNodes()) {

            if(node.getMinInOut() > maxMinInOut) {
                maxMinInOutNodes.clear();
                maxMinInOutNodes.add(node);
                maxMinInOut = node.getMinInOut();
            } else if(node.getMinInOut() == maxMinInOut) {
                maxMinInOutNodes.add(node);
            }
        }

        int minSize = Integer.MAX_VALUE;
        List<Cycle> minCycles = new ArrayList<>();
        for(Node node: maxMinInOutNodes) {
            List<Cycle> cycles = SimpleBFS.findBestCycles(graph, node, minSize);
            for(Cycle cycle: cycles) {
                if(cycle != null) {
                    if(cycle.size() < minSize) {
                        minCycles.clear();
                        minCycles.add(cycle);
                        minSize = cycle.size();
                    } else if(cycle.size() == minSize) {
                        minCycles.add(cycle);
                    }
                }
            }
        }

        //System.out.println("Found " + minCycles.size() + " min cycles");

        int maxCycleMinInOut = 0;
        Cycle bestCycle = null;
        for(Cycle minCycle : minCycles) {
            int cycleMinInOut = 0;
            for(Node node: minCycle.getNodes()) {
                cycleMinInOut += node.getMinInOut();
            }
            if(cycleMinInOut > maxCycleMinInOut) {
                bestCycle = minCycle;
                maxCycleMinInOut = cycleMinInOut;
            }
        }


        /* TODO 1. minInOut für jede Node setzen
           2. Bei Node mit dem größten anfangen, Simple BFS
           3. minSize setzen
           (4. frühzeitig abbrechen?)
           (5. packing Zeit optimieren, wenn sie überhand nimmt)
         */

        return bestCycle;
    }

    public static Cycle findShortestCycle(Graph graph){

        List<Cycle> cycles = graph.getPairCycles();
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

    public static List<Cycle> getAllShortestCycles(Graph graph) {

        List<Cycle> cycles = new ArrayList<>();

        // Find the best cycle for each node
        for (Node node : graph.getNodes()) {
            Cycle cycle = SimpleBFS.findBestCycle(graph, node, Integer.MAX_VALUE);
            if(cycle != null) cycles.add(cycle);
        }
        return cycles;
    }


    public static List<Cycle> findMultipleShortestCycles(Graph graph){

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

        for(Cycle cycle: minCycles) {
            cycle.cycleCount = 0;
            for(Node node: cycle.getNodes()) {
                cycle.cycleCount += node.cycleCount;
            }
            cycle.getNodes().sort(Comparator.comparing(Node::getCycleCount));
            Collections.reverse(cycle.getNodes());
        }

        List<Cycle> sol = new ArrayList<>();

        for(int i = 0; i < minCycles.size(); i++) {
            Cycle shortestCycle = Collections.max(minCycles, Comparator.comparing(x -> x.cycleCount));

            sol.add(shortestCycle);
            minCycles.remove(shortestCycle);
        }

        //Return
        return sol;
    }

}
