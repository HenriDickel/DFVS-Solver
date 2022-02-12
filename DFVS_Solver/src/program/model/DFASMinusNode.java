package program.model;

import java.util.ArrayList;
import java.util.List;

public class DFASMinusNode extends DFASNode {

    private DFASPlusNode outNode;
    public List<DFASPlusNode> inNodes = new ArrayList<>();

    public DFASMinusNode(Integer id) {
        super(id);
    }

    public void setOutNode(DFASPlusNode outNode) {
        this.outNode = outNode;
    }

    public void addInNode(DFASPlusNode inNode) {
        this.inNodes.add(inNode);
    }

    @Override
    public List<DFASNode> getOutNodes() {
        List<DFASNode> outNodes = new ArrayList<>();
        outNodes.add(outNode);
        return outNodes;
    }

    @Override
    public List<DFASNode> getInNodes() {
        List<DFASNode> nodes = new ArrayList<>();
        nodes.addAll(inNodes);
        return nodes;
    }

    @Override
    public Integer getTopologicalInCount() {
        return (int) inNodes.stream().filter(in -> in.topId == -1).count();
    }

    @Override
    public Integer getTopologicalOutCount() {
        return (outNode.topId == -1) ? 1 : 0;
    }
}
