package program.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

    // Main attributes
    public Integer id;

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
    public Integer depth;

    // Flower attributes
    public Integer petal;
    public Integer maxPetal;

    // Reduction attribute
    public Boolean updated = false;

    // Packing attribute
    public Boolean forbidden = false;
    public Boolean acyclic = false;
    public Boolean pNew = false;

    public Boolean marked = false;

    public int packingLevel = 0;

    public Node(Integer id) {
        this.id = id;
    }

    public List<Integer> getFullyConnectedIds() {
        return outIds.stream().filter(inIds::contains).collect(Collectors.toList());
    }

    public List<Integer> getOutIds() {
        return outIds;
    }

    public List<Integer> getInIds() {
        return inIds;
    }

    public void addOutId(Integer outId) {
        if(!outIds.contains(outId)) {
            outIds.add(outId);
        }
    }

    public void addInId(Integer inId) {
        if(!inIds.contains(inId)) {
            inIds.add(inId);
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

    public int getMinInOut() {
        return Math.min(inIds.size(), outIds.size());
    }

    public int getPackingLevel() {
        return packingLevel;
    }

    public Integer getBestDoubleEdge() {
        List<Integer> fullyConnected = outIds.stream().filter(inIds::contains).collect(Collectors.toList());
        if(fullyConnected.size() > 0) return fullyConnected.get(0);
        else return null;
    }

    public int getDoubleEdges(){
        return (int) outIds.stream().filter(inIds::contains).count();
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public Node copy() {
        Node copy = new Node(id);
        copy.maxPetal = maxPetal;
        copy.petal = petal;
        copy.packingLevel = packingLevel;
        // Clone out ids and in ids
        copy.outIds.addAll(outIds);
        copy.inIds.addAll(inIds);
        return copy;
    }

    /**
     * Returns true if node has edges and all of them are double edges
     * @return
     */
    public boolean hasOnlyDoubleEdges(){
        return inIds.size() + outIds.size() > 0 && inIds.size() == outIds.size() && inIds.containsAll(outIds) && outIds.containsAll(inIds);

    }

    public boolean hasDoubleEdge(Integer otherId) {
        return inIds.contains(otherId) && outIds.contains(otherId);
    }

    public boolean hasSelfEdge() {
        return outIds.contains(id);
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
