package program.model;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    public List<Node> nodes = new ArrayList<>();

    public void unvisitAllNodes() {
        nodes.forEach(node -> node.visitIndex = -1);
        nodes.forEach(node -> node.parent = null);
    }

    public void addArc(String from, String to){

        //Add nodes if not existing
        if(nodes.stream().noneMatch(x -> x.label.equals(from))) nodes.add(new Node(from));
        if(nodes.stream().noneMatch(x -> x.label.equals(to))) nodes.add(new Node(to));

        //Get Nodes
        Node a = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        Node b = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();

        //Add to neighbours
        a.addNeighbor(b);
    }

    public List<Node> getActiveNodes() {
        return nodes.stream().filter(node -> !node.deleted).collect(Collectors.toList());
    }

    public List<Node> getInactiveNodes() {
        return nodes.stream().filter(node -> node.deleted).collect(Collectors.toList());
    }

    public int getEdgeCount() {
        return getActiveNodes().stream().mapToInt(node -> node.getOutNeighbors().size()).sum();
    }

    public void fullyRemoveNode(Node nodeToRemove) {
        for (Node node : nodes) {
            node.removeNeighbor(nodeToRemove);
        }
        nodes.remove(nodeToRemove);
    }

    @Override
    public String toString() {
        List<String> nodesStrings = getActiveNodes().stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

}
