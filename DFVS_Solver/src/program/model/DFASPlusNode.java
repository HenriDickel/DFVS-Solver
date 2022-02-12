package program.model;

import java.util.ArrayList;
import java.util.List;

public class DFASPlusNode extends DFASNode {

    private DFASMinusNode inNode;
    public List<DFASMinusNode> outNodes = new ArrayList<>();

    public DFASPlusNode(Integer id) {
        super(id);
    }

    public void setInNode(DFASMinusNode inNode) {
        this.inNode = inNode;
    }

    public DFASMinusNode getInNode() {
        return inNode;
    }

    public void addOutNode(DFASMinusNode outNode) {
        this.outNodes.add(outNode);
    }

    @Override
    public List<DFASNode> getOutNodes() {
        List<DFASNode> nodes = new ArrayList<>();
        nodes.addAll(outNodes);
        return nodes;
    }

    @Override
    public List<DFASNode> getInNodes() {
        List<DFASNode> inNodes = new ArrayList<>();
        inNodes.add(inNode);
        return inNodes;
    }

    @Override
    public Integer getTopologicalInCount() {
        return (inNode.topId == -1) ? 1 : 0;
    }

    @Override
    public Integer getTopologicalOutCount() {
        return (int) outNodes.stream().filter(out -> out.topId == -1).count();
    }
}
