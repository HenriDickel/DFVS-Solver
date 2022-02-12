package program.model;

import java.util.*;

public class TopologicalOrdering {

    private List<DFASNode> orderedNodes = new LinkedList<>();

    public TopologicalOrdering() {}

    public List<DFASNode> getOrderedNodes() {
        return orderedNodes;
    }

    public void add(DFASNode node) {
        orderedNodes.add(node);
    }

    /**
     * Finds all back-edges in the topological ordering.
     * @return A list of node ids
     */
    public List<Integer> getS() {
        List<Integer> S = new ArrayList<>();
        for(DFASNode node: orderedNodes) {
            for(DFASNode in: node.getInNodes()) {
                if(in.topId > node.topId) {
                    S.add(in.id);
                }
            }
        }
        return S;
    }

    public int countCostEdges(DFASNode node) {
        int backEdgeCount = 0;
        for (DFASNode out: node.getOutNodes()) {
            if(out.topId < node.topId) { // If the out node is earlier in the ordering
                backEdgeCount++;
            }
        }
        for (DFASNode in: node.getInNodes()) {
            if(in.topId > node.topId) { // If the in node is later in the ordering
                backEdgeCount++;
            }
        }
        return backEdgeCount;
    }

    public void swapNodes(DFASNode A, DFASNode B) {
        Collections.swap(orderedNodes, A.topId, B.topId);
    }

    public void moveNode(DFASNode A) {

        for(DFASNode node: orderedNodes) {
            if(!node.equals(A) && node.topId >= A.topId) {
                node.topId++;
            }
        }
    }
}
