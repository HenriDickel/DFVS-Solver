package program.model;

import java.util.ArrayList;
import java.util.List;

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

    public int size() {
        return nodes.size();
    }

    public boolean contains(Node node) {
        return nodes.contains(node);
    }
}
