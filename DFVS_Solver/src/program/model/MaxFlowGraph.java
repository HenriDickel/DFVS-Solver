package program.model;

import java.util.*;

public class MaxFlowGraph {

    private Map<String, MaxFlowNode> nodes = new LinkedHashMap<>();

    public void addArc(String nodeLabel1, String nodeLabel2) {

        MaxFlowNode node1 = nodes.get(nodeLabel1);
        if(node1 == null) {
            node1 = new MaxFlowNode(nodeLabel1);
            nodes.put(nodeLabel1, node1);
        }
        MaxFlowNode node2 = nodes.get(nodeLabel2);
        if(node2 == null) {
            node2 = new MaxFlowNode(nodeLabel2);
            nodes.put(nodeLabel2, node2);
        }
        node1.addOutId(nodeLabel2);
        node2.addInId(nodeLabel1);
    }

    public List<MaxFlowNode> getNodes() {
        return nodes.values().stream().toList();
    }

    public int getNodeCount() {
        return nodes.size();
    }
}
