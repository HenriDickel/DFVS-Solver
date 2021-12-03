package program.model;

import java.util.*;

public class Instance {

    // Constants
    public final String NAME;
    public final int OPTIMAL_K;
    public final int N;
    public final int M;

    // Algorithm variables
    public List<Graph> subGraphs;
    public int solvedK = 0;
    public List<Node> S = new ArrayList<>(); // Result

    // Log variables
    public int startK = 0;
    public int preRemovedNodes = 0;
    public int removedFlowers = 0;
    public int recursiveSteps = 0;
    public float[] averageCycleSize;
    public int[] recursiveStepsPerK;

    public Instance(String NAME, Graph graph, int OPTIMAL_K) {
        this.NAME = NAME;
        this.subGraphs = Collections.singletonList(graph);
        this.OPTIMAL_K = OPTIMAL_K;
        this.N = graph.getNodeCount();
        this.M = graph.getEdgeCount();
    }
}
