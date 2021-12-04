package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class Reduction {


    public static List<Integer> applyRules(Graph graph) {

        List<Integer> reduceS = new ArrayList<>();
        List<Integer> updatedNodeIds;
        while(!(updatedNodeIds = graph.getUpdatedNodeIds()).isEmpty()) {
            for(Integer nodeId: updatedNodeIds) {
                Node node = graph.getNode(nodeId);
                if(node.getOutIds().contains(nodeId)) { // self loop
                    graph.removeNode(nodeId);
                    reduceS.add(nodeId);
                } else {
                    node.updated = false;
                }
            }
        }
        return reduceS;
    }

}
