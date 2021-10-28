package program;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    //Name of the graph for logging
    public String name;

    private List<Node> nodes = new ArrayList<>();

    public Graph(String name) {
        this.name = name;
    }

    public void unvisitAllNodes() {
        nodes.forEach(node -> node.visitIndex = -1);
    }

    public void addArc(String from, String to){

        //Add nodes if not existing
        if(nodes.stream().noneMatch(x -> x.label.equals(from))) nodes.add(new Node(from));
        if(nodes.stream().noneMatch(x -> x.label.equals(to))) nodes.add(new Node(to));

        //Get Nodes
        Node a = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        Node b = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();

        //Add to neighbours
        a.addNeighbour(b);
    }

    public List<Node> getActiveNodes() {
        return nodes.stream().filter(node -> !node.deleted).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        List<String> nodesStrings = getActiveNodes().stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

}
