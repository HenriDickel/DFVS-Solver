package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

public class FullBFS {

    public static Cycle findBestCycle(Graph g){

        // Result
        Cycle bestCycle = null;

        // Find the best cycle for each node
        for(Node node : g.getActiveNodes()){

            //Get the best cycle starting at current node
            Cycle cycle = SimpleBFS.findBestCycle(g, node);

            // Replace the best when found better one
            if(cycle != null) {
                if(bestCycle == null || cycle.unforbiddenSize() < bestCycle.unforbiddenSize()) {
                    bestCycle = cycle;
                }
            }
        }
        if(bestCycle == null) {
            throw new RuntimeException("Full BFS didn't found a cycle!");
        }
        return bestCycle;
    }
}
