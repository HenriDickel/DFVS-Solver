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

    public void removeNode(Node nodeToRemove) {
        for (Node node : getNodes()) {
            node.removeOutId(nodeToRemove.id);
            node.removeInId(nodeToRemove.id);
        }
        nodes.remove(nodeToRemove.id);
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

    public void addArc(String a, String b) {

        // Add nodes if not existing
        int nextId = nodes.size();
        if(nodes.values().stream().noneMatch(x -> x.label.equals(a))) nodes.put(nextId, new Node(nextId, a));
        nextId = nodes.size();
        if(nodes.values().stream().noneMatch(x -> x.label.equals(b))) nodes.put(nextId, new Node(nextId, b));

        //Get Nodes
        Node aNode = nodes.values().stream().filter(x -> x.label.equals(a)).findFirst().get();
        Node bNode = nodes.values().stream().filter(x -> x.label.equals(b)).findFirst().get();

        //Add to neighbours
        aNode.addOutId(bNode.id);
        bNode.addInId(aNode.id);
    }
}
