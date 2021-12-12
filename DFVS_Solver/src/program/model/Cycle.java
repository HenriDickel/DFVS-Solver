package program.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cycle {
    private List<Node> nodes = new ArrayList<>();

    public Cycle(Node start) {
        nodes.add(start);
    }

    public Cycle(Node a, Node b) {
        nodes.add(a);
        nodes.add(b);
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int size() {
        return nodes.size();
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }

    @Override
    public String toString() {
        List<String> nodeLabels = nodes.stream().map(Node::toString).collect(Collectors.toList());
        return String.join("-", nodeLabels);
    }
}
