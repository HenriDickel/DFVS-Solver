package program.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cycle {

    private List<Node> nodes = new ArrayList<>();

    public Cycle(Node start) {
        nodes.add(start);
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int unforbiddenSize() {
        return (int) nodes.stream().filter(node -> node.forbidden == Integer.MAX_VALUE).count();
    }

    public int size() {
        return nodes.size();
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }

    @Override
    public String toString() {
        List<String> nodeLabels = nodes.stream().map(node -> node.label).collect(Collectors.toList());
        return String.join("-", nodeLabels);
    }
}
