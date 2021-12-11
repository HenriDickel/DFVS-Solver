package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

public abstract class CyclePacking {

    public static boolean checkLowerBounds(Graph graph, int k) {

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

    public static int getLowerBound(Graph graph) {
        int lowerBound = 0;
        Cycle cycle;
        while ((cycle = LightBFS.findShortestCycle(graph)) != null) {
            for (Node node : cycle.getNodes()) {
                graph.removeNode(node.id);
            }
            lowerBound++;
        }
        return lowerBound;
    }
}
