package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FullBFS {

    public static Cycle findBestCycle(Graph graph) {

        List<Cycle> minCycles = graph.getPairCycles();

        // When there are no cycles of size 2, look for shortest cycles with BFS
        if(minCycles.size() == 0) {
            // Find nodes with highest minInOut
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

            // Find shortest cycles for minInOut
            int minSize = Integer.MAX_VALUE;
            minCycles = new ArrayList<>();
            for(Node node: maxMinInOutNodes) {

                Cycle cycle = SimpleBFS.findBestCycle(graph, node, minSize);
                if(cycle != null) {
                    minCycles.add(cycle);
                    minSize = cycle.size();
                }
            }
            if(minCycles.size() == 0) {
                return findShortestCycle(graph);
            }

            // Filter out all cycles which are longer than the min size
            final int finalMinSize = minSize;
            minCycles = minCycles.stream().filter(cycle -> cycle.size() == finalMinSize).collect(Collectors.toList());
        }

        // Find the cycle with the highest minInOut sum
        double maxMinInOutSum = 0;
        Cycle bestCycle = null;
        for(Cycle cycle : minCycles) {
            double minInOutSum = 0;
            for(Node node: cycle.getNodes()) {
                minInOutSum += node.getMinInOut();
            }
            minInOutSum /= cycle.getNodes().size();
            if(minInOutSum > maxMinInOutSum) {
                bestCycle = cycle;
                maxMinInOutSum = minInOutSum;
            }
        }

        // Sort nodes in cycle
        //bestCycle.getNodes().sort(Comparator.comparing(Node::getMinInOut));
        //Collections.reverse(bestCycle.getNodes());

        //Sort
        bestCycle.getNodes().sort(Comparator.comparing(Node::getDoubleEdges));

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


    public static List<Cycle> findMultipleShortestCycles(Graph graph, boolean sortResults){


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

        if(sortResults){
            minCycles.sort(Comparator.comparing(Cycle::getMinInOutSum));
            Collections.reverse(minCycles);
        }

        //Return
        return minCycles;
    }

    public static List<Cycle> findSomeCyclesFast(Graph graph, float groupSize, boolean orderNodes){

        List<Cycle> cycles = graph.getPairCycles();
        int minSize = 2;

        //Sort Nodes
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        if(orderNodes){
            nodes.sort(Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));
        }
        else{
            Collections.shuffle(nodes);
        }

        // When there are no cycles of size 2, look for shortest cycles with BFS
        if(cycles.size() == 0) {
            minSize = Integer.MAX_VALUE;
            for(int i = 0; i <= (float) nodes.size() * groupSize; i++){

                //Precision needs to be smaller than 1
                if(i == nodes.size()) break;

                //Get Node
                Node node = nodes.get(i);
                Cycle cycle = SimpleBFS.findBestCycle(graph, node, minSize);

                // Replace the min branch size when found better one
                if (cycle != null) {
                    cycles.add(cycle);
                    minSize = Math.min(cycle.size(), minSize);
                }
            }
        }
        else{
            Collections.shuffle(cycles);
            int maxIndex = (int) (cycles.size() * groupSize);
            if(maxIndex > 0) cycles = cycles.subList(0, maxIndex);
        }

        // Filter out all cycles which are longer than the min branch size
        final int finalMinSize = minSize;
        return cycles.stream().filter(cycle -> cycle.size() == finalMinSize).collect(Collectors.toList());

    }
}
