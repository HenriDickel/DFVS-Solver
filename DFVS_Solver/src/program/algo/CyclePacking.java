package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.List;

public abstract class CyclePacking {

    public static boolean checkLowerBounds(Graph graph, int k) {
        if(findCyclePairs(graph,k)){
            return true;
        }else{
            return  findDisjointCycles(graph,k);
        }
        //TODO 2-Cycle Subraphs
        //TODO Bipartit Check
        //TODO Ford Fulkerson for those
        //TODO Maximal Matching
        //TODO Fully Connected up to 5 Node

    }

    public static boolean findDisjointCycles(Graph graph, int k){
        Graph packingCopy = graph.copy();
        int lowerBound = 0;
        Cycle cycle;
        while ((cycle = LightBFS.findShortestCycle(packingCopy)) != null) {
            for (Node node : cycle.getNodes()) {
                packingCopy.removeNode(node.id);
            }
            lowerBound++;
            if(lowerBound > k){
                return true;
            }
        }
        return false;
    }


    public static boolean findCyclePairs(Graph graph, int k){
        Graph copy = graph.copy();
        int lowerBound = 0;
        Integer[] delete;
        while((delete=find2Cycle(copy)).length!=0) {
            lowerBound++;
            if(lowerBound>k){
                return true;
            }
            copy.removeNode(delete[0]);
            copy.removeNode(delete[1]);
        }
        return false;
    }

    private static Integer[] find2Cycle(Graph graph) {
        for (Node node : graph.getNodes()) {
            if (node.getInIds().size() < node.getOutIds().size()) {
                for (int i = 0; i < node.getInIds().size(); i++) {
                    if (node.getOutIds().contains(node.getInIds().get(i))) {
                        return new Integer[]{node.id, node.getInIds().get(i)};
                    }
                }
            } else {
                for (int i = 0; i < node.getOutIds().size(); i++) {
                    if (node.getInIds().contains(node.getOutIds().get(i))) {
                        return new Integer[]{node.id, node.getOutIds().get(i)};
                    }
                }
            }
        }
        return new Integer[]{};
    }

    private static Cycle getTriangle(Graph graph, int minCycleCountPerNode) {
        int count = 0;

        Cycle bestTriangle = null;
        int minCycleCountPerTri = Integer.MAX_VALUE;

        for(Node node: graph.getNodes()) {
            for(Integer aId: node.getOutIds()) {
                if (node.getInIds().contains(aId)) { // node <-> a
                    for(Integer bId: node.getOutIds()) {
                        if(node.getInIds().contains(bId)) { // b <-> node <-> a
                            Node a = graph.getNode(aId);
                            Node b = graph.getNode(bId);
                            if(b.getOutIds().contains(aId) && b.getInIds().contains(aId)) { // fully connected triangle
                                int cycleCount = node.cycleCount + a.cycleCount + b.cycleCount;
                                if((node.cycleCount == minCycleCountPerNode || a.cycleCount == minCycleCountPerNode || b.cycleCount == minCycleCountPerNode) && cycleCount < minCycleCountPerTri) {
                                    minCycleCountPerTri = cycleCount;
                                    bestTriangle = new Cycle(node, a, b);
                                }
                                count += 1;
                            }
                        }
                    }
                }
            }
        }
        return bestTriangle;
    }

    private static int removeNextCycle(Graph graph) {

        // Get pair cycles
        List<Cycle> pairCycles = graph.getPairCycles();
        if(!pairCycles.isEmpty()) {
            // Set cycle count for every node
            int minCycleCountPerNode = Integer.MAX_VALUE;
            for(Node node: graph.getNodes()) {
                node.cycleCount = 0;
                for(Cycle pairCycle: pairCycles) {
                    if(pairCycle.contains(node)) {
                        node.cycleCount++;
                    }
                }
                if(node.cycleCount > 0)
                    minCycleCountPerNode = Math.min(minCycleCountPerNode, node.cycleCount);
            }

            // Look for fully connected triangles
            Cycle triangle = getTriangle(graph, minCycleCountPerNode);
            if(triangle != null) {
                graph.removeNode(triangle.get(0).id);
                graph.removeNode(triangle.get(1).id);
                graph.removeNode(triangle.get(2).id);
                return 2;
            }

            // Find the min cycle
            int minCycleCount = Integer.MAX_VALUE;
            Cycle bestCycle = null;
            for(Cycle pairCycle : pairCycles) {
                int cycleCount = 0;
                Node a = pairCycle.get(0);
                Node b = pairCycle.get(0);
                for(Node node: pairCycle.getNodes()) {
                    cycleCount += node.cycleCount;
                }
                if((a.cycleCount == minCycleCountPerNode || b.cycleCount == minCycleCountPerNode) ) { // If new max found, replace the current best cycle
                    bestCycle = pairCycle;
                    minCycleCount = cycleCount;
                }
            }

            // Throw exception, when no best cycle is found
            if(bestCycle == null) {
                throw new RuntimeException("Cycle packing didn't found a cycle!");
            }

            for (Node node : bestCycle.getNodes()) {
                graph.removeNode(node.id);
            }
            return 1;
        }

        // Look for cycle to remove (if no pair cycles found)
        Cycle cycle = LightBFS.findShortestCycle(graph);
        if(cycle != null) {
            for (Node node : cycle.getNodes()) {
                graph.removeNode(node.id);
            }
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean checkLowerBound(Graph graph, int k) {
        Graph packingCopy = graph.copy();
        int lowerBound = 0;
        int addToLowerBound;
        while((addToLowerBound = removeNextCycle(packingCopy)) > 0) {
            lowerBound += addToLowerBound;
            List<Integer> reduceS = Reduction.applyRules(packingCopy, false);
            lowerBound += reduceS.size();
            if(lowerBound > k){
                return true;
            }
        }
        return false;
    }

    public static int getLowerBound(Graph graph) {

        int lowerBound = 0;
        int bound;
        while((bound = removeNextCycle(graph)) > 0) {
            lowerBound += bound;
            List<Integer> reduceS = Reduction.applyRules(graph, false);
            lowerBound += reduceS.size();
        }
        return lowerBound;
    }
}
