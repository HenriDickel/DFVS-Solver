package program.model;

import java.util.*;

public class TopologicalOrdering {

    private Map<Integer, Node> nodeMap;
    private List<Node> orderedNodes = new LinkedList<>();

    public TopologicalOrdering(Graph graph) {
        nodeMap = graph.getNodeMap();
    }

    public void add(Node node) {
        orderedNodes.add(node);
    }

    public List<Node> getNodes() {
        return orderedNodes;
    }

    public List<Integer> getS() {
        List<Integer> S = new ArrayList<>();
        for(Node node: orderedNodes) {
            for (Integer outId: node.getOutIds()) {
                Node out = nodeMap.get(outId);
                if(out.topologicalId < node.topologicalId) { // If the out node is earlier in the ordering
                    S.add(out.topologicalId);
                }
            }
        }
        return S;
    }

    public int countCostEdges(Node node) {
        int backEdgeCount = 0;
        for (Integer outId: node.getOutIds()) {
            Node out = nodeMap.get(outId);
            if(out.topologicalId < node.topologicalId) { // If the out node is earlier in the ordering
                backEdgeCount++;
            }
        }
        for (Integer inId: node.getInIds()) {
            Node in = nodeMap.get(inId);
            if(in.topologicalId > node.topologicalId) { // If the in node is later in the ordering
                backEdgeCount++;
            }
        }
        return backEdgeCount;
    }

    public void swapNodes(Node A, Node B) {
        Collections.swap(orderedNodes, A.topologicalId, B.topologicalId);
    }

    public void moveNode(Node A, int position) {
        orderedNodes.remove(A);
        orderedNodes.add(position, A);
    }
}
