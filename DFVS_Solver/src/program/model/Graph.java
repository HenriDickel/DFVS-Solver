package program.model;


import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();

    public List<Node> getNodes() {
        return nodes.values().stream().toList();
    }

    public Map<Integer, Node> getNodeMap() {
        return nodes;
    }

    public Node getNode(int id) {
        Node node = nodes.get(id);
        if(node == null) throw new RuntimeException("Couldn't find node with id " + id);
        return node;
    }

    public boolean hasNode(int id) {
        return nodes.get(id) != null;
    }

    public List<Node> getOutNodes(Node node) {
        return node.getOutIds().stream().map(this::getNode).collect(Collectors.toList());
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return nodes.values().stream().map(Node::getOutIdCount).mapToInt(Integer::valueOf).sum();
    }

    public Cycle getFirstPairCycle() {
        for (Node node : getNodes()) {
            for(Integer outId: node.getOutIds()) {
                if(node.getInIds().contains(outId)) {
                    return new Cycle(node, getNode(outId));
                }
            }
        } return null;
    }

    public List<Cycle> getPairCycles() {
        List<Cycle> cycles = new ArrayList<>();
        // Look for all cycles of size 2
        for (Node node : getNodes()) {
            for(Integer outId: node.getOutIds()) {
                if(node.getInIds().contains(outId)) {
                    cycles.add(new Cycle(node, getNode(outId)));
                }
            }
        }
        return cycles;
    }

    public void resetBFS() {
        nodes.values().forEach(node -> node.visitIndex = -1);
        nodes.values().forEach(node -> node.parent = null);
    }

    public Graph copySorted() {
        Graph copyGraph = new Graph();

        List<Node> copyNodes = new LinkedList<>();
        for (Node node : getNodes()) {
            Node copyNode = node.copy();
            copyNodes.add(copyNode);
        }

        copyNodes.sort(Comparator.comparing(Node::getMinInOut));
        Collections.reverse(copyNodes);

        for(Node copyNode: copyNodes) {
            copyGraph.nodes.put(copyNode.id, copyNode);
        }
        return copyGraph;
    }

    public Graph copy() {
        Graph copyGraph = new Graph();

        for (Node node : getNodes()) {
            Node copyNode = node.copy();
            copyGraph.nodes.put(copyNode.id, copyNode);
        }
        return copyGraph;
    }

    public void setAllNodesUpdated() {
        nodes.values().forEach(node -> node.updated = true);
    }

    public List<Integer> getUpdatedNodeIds() {
        return nodes.values().stream().filter(node -> node.updated).map(node -> node.id).collect(Collectors.toList());
    }

    /**
     * Fully removes a node from the graph. Also removes the node id from the neighbors inIds and outIds.
     * @param nodeId The node id.
     */
    public void removeNode(Integer nodeId) {
        Node node = nodes.get(nodeId);
        if(node == null) return;
        for(Integer outId: node.getOutIds()) {
            Node out = getNode(outId);
            out.removeInId(nodeId);
            out.updated = true;
        }
        for(Integer inId: node.getInIds()) {
            Node in = getNode(inId);
            in.removeOutId(nodeId);
            in.updated = true;
        }
        nodes.remove(nodeId);
    }

    public void removeForbiddenNodes(List<Integer> forbiddenNodeIds) {
        for(Integer forbiddenId: forbiddenNodeIds) {
            removeForbiddenNode(forbiddenId);
        }
    }

    private void removeForbiddenNode(Integer forbiddenId) {
        Node forbidden = getNode(forbiddenId);
        for(Node node: getNodes()) {
            if(node.getOutIds().contains(forbiddenId)) {
                for(Node out: getOutNodes(forbidden)) {
                    out.addInId(node.id);
                    node.addOutId(out.id);
                }
            }
        }
        removeNode(forbidden.id);
    }

    public void addInitialNode(Node initialNode) {
        Node add = new Node(initialNode.id);
        for(Integer outId: initialNode.getOutIds()) {
            if(hasNode(outId)) {
                Node out = getNode(outId);
                add.addOutId(outId);
                out.addInId(add.id);
            }
        }
        for(Integer inId: initialNode.getInIds()) {
            if(hasNode(inId)) {
                Node in = getNode(inId);
                add.addInId(inId);
                in.addOutId(add.id);
            }
        }
        nodes.put(add.id, add);
    }

    public void addArc(Integer nodeId1, Integer nodeId2) {

        Node node1 = nodes.get(nodeId1);
        if(node1 == null) {
            node1 = new Node(nodeId1);
            nodes.put(nodeId1, node1);
        }
        Node node2 = nodes.get(nodeId2);
        if(node2 == null) {
            node2 = new Node(nodeId2);
            nodes.put(nodeId2, node2);
        }
        node1.addOutId(nodeId2);
        node2.addInId(nodeId1);
    }
}
