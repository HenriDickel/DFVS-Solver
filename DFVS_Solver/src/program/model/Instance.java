package program.model;

import java.util.*;
import java.util.stream.Collectors;

public class Instance {

    // Constants
    public final String NAME;
    public final int OPTIMAL_K;
    public int N;
    public int M;

    // Algorithm variables
    public List<Graph> subGraphs;
    public List<Integer> S = new ArrayList<>(); // Result

    // Log variables
    public int startK = 0;
    public int preRemovedNodes = 0;
    public int removedFlowers = 0;
    public int recursiveSteps = 0;
    public float[] averageCycleSize;
    public int[] recursiveStepsPerK;
    public int solvedK = 0;
    public int packingSize = 0;
    public int heuristicSize = 0;

    public int numConstraints = 0;

    public Instance(String NAME, Graph graph, int OPTIMAL_K) {
        this.NAME = NAME;
        this.subGraphs = Collections.singletonList(graph);
        this.OPTIMAL_K = OPTIMAL_K;
        this.N = graph.getNodeCount();
        this.M = graph.getEdgeCount();
    }

    public int getCurrentN(){
        return subGraphs.stream().mapToInt(Graph::getNodeCount).sum();

    }

    public List<Node> getAllNodes(){
        return subGraphs.stream().map(Graph::getNodes).flatMap(List::stream).collect(Collectors.toList());
    }

    public int getCurrentM(){
        List<Node> nodes = subGraphs.stream().map(Graph::getNodes).flatMap(List::stream).collect(Collectors.toList());
        return getCurrentN() == 0 ? 0 : nodes.stream().map(Node::getOutIdCount).reduce(0, Integer::sum);
    }

}
