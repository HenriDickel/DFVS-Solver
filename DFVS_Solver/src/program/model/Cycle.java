package program.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cycle extends Component {

    private final List<Node> nodes = new ArrayList<>();

    public Cycle(Node... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node get(int index) {
        return nodes.get(index);
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
