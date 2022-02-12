package program.model;

import java.util.List;

public abstract class DFASNode {

    public Integer id;
    public Integer topId;

    public DFASNode(Integer id) {
        this.id = id;
    }

    public abstract List<DFASNode> getOutNodes();

    public abstract List<DFASNode> getInNodes();

    public abstract Integer getTopologicalInCount();

    public abstract Integer getTopologicalOutCount();

    public Integer cost() {
        int cost = 0;
        cost += getInNodes().stream().filter(in -> in.topId > topId).count();
        cost += getOutNodes().stream().filter(out -> out.topId < topId).count();
        return cost;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
