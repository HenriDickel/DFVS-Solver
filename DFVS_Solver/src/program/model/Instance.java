package program.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Instance {

    // Constants
    public final String NAME;
    public final int OPTIMAL_K;
    public final int N;
    public final int M;

    // Algorithm variables
    public List<Graph> subGraphs;
    public int solvedK = 0;
    public List<Node> S = new ArrayList<>();    //Result

    public Instance(String NAME, Graph graph, int OPTIMAL_K) {
        this.NAME = NAME;
        this.subGraphs = Collections.singletonList(graph);
        this.OPTIMAL_K = OPTIMAL_K;
        this.N = graph.getActiveNodes().size();
        this.M = graph.getEdgeCount();
    }
}
