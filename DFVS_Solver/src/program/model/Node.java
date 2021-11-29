package program.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

    public String label;
    public boolean deleted = false;
    private final List<Node> outNeighbours;
    private final List<Node> inNeighbours;
    public int weight;
    public int forbidden = Integer.MAX_VALUE;

    // Attributes for Tarjan
    public int index;
    public int lowLink;
    public boolean onStack;

    // Attributes for BFS
    public Node parent;
    public int visitIndex;

    //Flower
    public int petal;
    public int maxPetal;

    public Node(String label) {
        this.label = label;
        outNeighbours = new ArrayList<>();
        inNeighbours = new ArrayList<>();
    }

    public void addOutNeighbor(Node neighbor) {
        if(!outNeighbours.contains(neighbor)) {
            outNeighbours.add(neighbor);
        }
    }

    public void addInNeighbor(Node neighbor) {
        if(!inNeighbours.contains(neighbor)) {
            inNeighbours.add(neighbor);
        }
    }

    public void removeOutNeighbor(Node neighbor) {
        outNeighbours.remove(neighbor);
    }

    public void removeInNeighbor(Node neighbor) {
        inNeighbours.remove(neighbor);
    }

    public List<Node> getOutNeighbors() {
        return outNeighbours.stream().filter(x -> !x.deleted).collect(Collectors.toList());
    }

    public List<Node> getInNeighbors() {
        return inNeighbours.stream().filter(x -> !x.deleted).collect(Collectors.toList());
    }

    public void delete() {
        deleted = true;
    }

    public void unDelete() {
        deleted = false;
    }

    public int getMaxPetal() {
        return maxPetal;
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
