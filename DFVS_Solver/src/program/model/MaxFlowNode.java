package program.model;

import java.util.ArrayList;
import java.util.List;

public class MaxFlowNode {

    public String label;

    // Neighbor indices
    private final List<String> outLabels = new ArrayList<>();
    private final List<String> inLabels = new ArrayList<>();

    public MaxFlowNode(String label) {
        this.label = label;
    }

    public List<String> getOutLabels() {
        return outLabels;
    }

    public void addOutId(String label) {
        if(!outLabels.contains(label)) {
            outLabels.add(label);
        }
    }

    public void addInId(String label) {
        if(!inLabels.contains(label)) {
            inLabels.add(label);
        }
    }
}
