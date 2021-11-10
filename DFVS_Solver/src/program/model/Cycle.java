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

    @Override
    public String toString() {
        List<String> nodeLabels = nodes.stream().map(node -> node.label).collect(Collectors.toList());
        return String.join("-", nodeLabels);
    }
}
