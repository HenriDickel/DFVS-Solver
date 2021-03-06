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

    public boolean containsId(Integer id) {
        for(Node node: nodes) {
            if(node.id.equals(id)) return true;
        }
        return false;
    }

    public boolean isFullyConnected(Integer otherId) {
        for(Node node: nodes) {
            if(node.id.equals(otherId)) return false;
            if(!node.getOutIds().contains(otherId)) return false;
            if(!node.getInIds().contains(otherId)) return false;
        }
        return true;
    }

    public Cycle copy() {
        Cycle copy = new Cycle();
        for(Node node: nodes) {
            copy.add(node.copy());
        }
        copy.setK(getK());
        return copy;
    }

    @Override
    public String toString() {
        List<String> nodeLabels = nodes.stream().map(Node::toString).collect(Collectors.toList());
        return String.join("-", nodeLabels);
    }
}
