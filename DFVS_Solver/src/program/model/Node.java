package program.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

    public String label;
    public boolean deleted = false;
    private final List<Node> outNeighbours;
    public int weight;
    public int forbidden = Integer.MAX_VALUE;

    // Attributes for Tarjan
    public int index;
    public int lowLink;
    public boolean onStack;

    // Attributes for BFS
    public Node parent;
    public int visitIndex;

    public Node(String label) {
        this.label = label;
        outNeighbours = new ArrayList<>();
    }

    public void addNeighbor(Node neighbor) {
        outNeighbours.add(neighbor);
    }

    public void removeNeighbor(Node neighbor) {
        outNeighbours.remove(neighbor);
    }

    public List<Node> getOutNeighbors() {
        return outNeighbours.stream().filter(x -> !x.deleted).collect(Collectors.toList());
    }

    public void delete() {
        deleted = true;
    }

    public void unDelete() {
        deleted = false;
    }

    @Override
    public String toString() {
        String nodeString = label;
        List<String> outNeighborsStrings = outNeighbours.stream().map(x -> x.label).collect(Collectors.toList());
        String outNeighborsStringJoined = String.join(", ", outNeighborsStrings);

        if (!outNeighborsStrings.isEmpty()) outNeighborsStringJoined = " {" + outNeighborsStringJoined + "}";

        return nodeString + outNeighborsStringJoined;
    }

}
