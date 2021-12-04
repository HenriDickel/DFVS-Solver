package program.model;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph {

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();

    public List<Node> getNodes() {
        return nodes.values().stream().toList();
    }

    public Node getNode(int id) {

        Node node = nodes.get(id);
        if(node == null) throw new RuntimeException("Couldn't find node with id " + id);
        return node;
    }

    public List<Node> getOutNodes(Node node) {
        return node.getOutIds().stream().map(this::getNode).collect(Collectors.toList());
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return (int) nodes.values().stream().map(Node::getOutIdCount).count();
    }

    public void resetBFS() {
        nodes.values().forEach(node -> node.visitIndex = -1);
        nodes.values().forEach(node -> node.parent = null);
    }

    public Graph copy() {
        Graph copyGraph = new Graph();

        for (Node node : getNodes()) {
            Node copyNode = node.copy();
            copyGraph.nodes.put(copyNode.id, copyNode);
        }
        return copyGraph;
    }

    public List<Integer> getUpdatedNodeIds() {
        return nodes.values().stream().filter(node -> node.updated).map(node -> node.id).collect(Collectors.toList());
    }

    public void removeNode(Integer nodeId) {
        Node node = nodes.get(nodeId);
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
                node.removeOutId(forbidden.id);
            }
            if(node.getInIds().contains(forbidden.id)) {
                // Transferring edges already happened above
                node.removeInId(forbidden.id);
            }
        }
        nodes.remove(forbidden.id);
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
