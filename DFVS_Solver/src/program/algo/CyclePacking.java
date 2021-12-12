package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

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
