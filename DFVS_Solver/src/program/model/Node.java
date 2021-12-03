package program.model;

import java.util.ArrayList;
import java.util.List;

public class Node {

    // Main attributes
    public Integer id;
    public String label;

    // Neighbor indices
    private final List<Integer> outIds = new ArrayList<>();
    private final List<Integer> inIds = new ArrayList<>();

    // BFS attributes
    public Integer visitIndex;
    public Node parent;
    public Integer cycleCount;

    // Tarjan attributes
    public Integer index;
    public Integer lowLink;
    public Boolean onStack;

    // Flower attributes
    public Integer petal;
    public Integer maxPetal;

    public Node(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public List<Integer> getOutIds() {
        return outIds;
    }

    public List<Integer> getInIds() {
        return inIds;
    }

    public void addOutId(int id) {
        if(!outIds.contains(id)) {
            outIds.add(id);
        }
    }

    public void addInId(int id) {
        if(!inIds.contains(id)) {
            inIds.add(id);
        }
    }

    public void removeOutId(Integer id) {
        outIds.remove(id);
    }

    public void removeInId(Integer id) {
        inIds.remove(id);
    }

    public int getOutIdCount() {
        return outIds.size();
    }

    public int getInIdCount() {
        return inIds.size();
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public Node copy() {
        Node copy = new Node(id, label);
        copy.maxPetal = maxPetal;
        copy.petal = petal;
        // Clone out ids and in ids
        copy.outIds.addAll(outIds);
        copy.inIds.addAll(inIds);
        return copy;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
