package program.heuristics;

import program.algo.Preprocessing;
import program.algo.Reduction;
import program.log.Log;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.model.TopologicalOrdering;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DFASHeuristicSolver {

    public static void solveInstance(Instance instance) {

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create DFAS arc graph
        Graph arcGraph = new Graph();

        int runningId = 0;
        Map<Integer, Integer> plusIds = new HashMap<>();
        Map<Integer, Integer> minusIds = new HashMap<>();

        // Add minus -> plus edge for all nodes
        for(Node node: initialGraph.getNodes()) {
            int minusId = runningId;
            runningId++;
            int plusId = runningId;
            runningId++;
            minusIds.put(node.id, minusId);
            plusIds.put(node.id, plusId);
            arcGraph.addArc(minusId, plusId);
        }

        // Add plus -> minus edge for all edges
        for(Node node: initialGraph.getNodes()) {
            int plusId = plusIds.get(node.id);
            for(Integer outId: node.getOutIds()) {
                int minusId = minusIds.get(outId);
                arcGraph.addArc(plusId, minusId);
            }
        }

        instance.N = arcGraph.getNodeCount();
        instance.M = arcGraph.getEdgeCount();

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(arcGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Log preprocessing
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        //destroyCycles(instance);
        topologicalOrder(instance);

        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size(), false);
    }

    private static void topologicalOrder(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            subGraph.resetTopologicalOrdering();

            // Sort by min in count: 8.104
            //sortByMinIn(subGraph);

            // Sort by max out count: 5.505
            //sortByMaxOutCount(subGraph);

            // Sort by min in count -> max out count: 1.795
            //TopologicalOrdering ordering = sortByMinInMaxOut(subGraph);

            // Sort by min in count -> max out count + local search: 1.498
            TopologicalOrdering ordering = sortByMinInMaxOut(subGraph);
            Log.debugLog(instance.NAME, "Heuristic size after sorting: " + ordering.getS().size());
            localSearchSwapAll(ordering);
            Log.debugLog(instance.NAME, "Heuristic size after local search: " + ordering.getS().size());

            // Sort by max out count -> min in count: 5.505
            //sortByMaxOutMinIn(subGraph);

            // Add backward edges to S
            instance.S.addAll(ordering.getS());
        }
    }

    private static void sortByMinIn(Graph graph) {

        List<Node> unorderedNodes = new ArrayList<>(graph.getNodes());

        for(int i = 0; i < graph.getNodeCount(); i++) {

            Node nextNode = unorderedNodes.get(0);
            int inCountMin = Integer.MAX_VALUE;
            for(Node node: unorderedNodes) {
                // Count number of in edges from nodes
                int inCount = 0;
                for(Integer inId: node.getInIds()) {
                    Node in = graph.getNode(inId);
                    if (in.topologicalId == -1) {
                        inCount++;
                    }
                }
                if(inCount < inCountMin) {
                    inCountMin = inCount;
                    nextNode = node;
                }
            }
            nextNode.topologicalId = i;
            unorderedNodes.remove(nextNode);
        }
    }

    private static void sortByMaxOutCount(Graph graph) {

        List<Node> unorderedNodes = new ArrayList<>(graph.getNodes());

        for(int i = 0; i < graph.getNodeCount(); i++) {

            Node nextNode = unorderedNodes.get(0);
            int outCountMax = 0;
            for(Node node: unorderedNodes) {
                // Count number of out edges to nodes
                int outCount = 0;
                for(Integer outId: node.getOutIds()) {
                    Node out = graph.getNode(outId);
                    if (out.topologicalId == -1) {
                        outCount++;
                    }
                }
                if(outCount > outCountMax) {
                    outCountMax = outCount;
                    nextNode = node;
                }
            }
            nextNode.topologicalId = i;
            unorderedNodes.remove(nextNode);
        }
    }

    private static TopologicalOrdering sortByMinInMaxOut(Graph graph) {

        TopologicalOrdering ordering = new TopologicalOrdering(graph);
        List<Node> unorderedNodes = new LinkedList<>(graph.getNodes());

        for(int i = 0; i < graph.getNodeCount(); i++) {

            Node nextNode = unorderedNodes.get(0);
            int minInCount = Integer.MAX_VALUE;
            int maxOutCount = 0;
            for(Node node: unorderedNodes) {
                // Count number of in edges from nodes
                int inCount = 0;
                for(Integer inId: node.getInIds()) {
                    Node in = graph.getNode(inId);
                    if (in.topologicalId == -1) {
                        inCount++;
                    }
                }
                // Count number of out edges to unordered nodes
                int outCount = 0;
                for(Integer outId: node.getOutIds()) {
                    Node out = graph.getNode(outId);
                    if (out.topologicalId == -1) {
                        outCount++;
                    }
                }
                if(inCount < minInCount) {
                    minInCount = inCount;
                    maxOutCount = outCount;
                    nextNode = node;
                } else if(inCount == minInCount && outCount > maxOutCount) {
                    maxOutCount = outCount;
                    nextNode = node;
                }
            }
            nextNode.topologicalId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static int countBackEdges(Graph graph, Node node) {
        int backEdgeCount = 0;
        for (Integer outId: node.getOutIds()) {
            Node out = graph.getNode(outId);
            if(out.topologicalId < node.topologicalId) { // If the out node is earlier in the ordering
                backEdgeCount++;
            }
        }
        for (Integer inId: node.getInIds()) {
            Node in = graph.getNode(inId);
            if(in.topologicalId > node.topologicalId) { // If the in node is later in the ordering
                backEdgeCount++;
            }
        }
        return backEdgeCount;
    }

    private static void localSearchSwapAll(TopologicalOrdering ordering) {

        boolean improvementFound = true;
        while(improvementFound) {
            improvementFound = false;
            for (Node A : ordering.getNodes()) {
                for (Node B : ordering.getNodes()) {
                    int backEdgeCount = ordering.countCostEdges(A) + ordering.countCostEdges(B);
                    int topologicalIdA = A.topologicalId;
                    A.topologicalId = B.topologicalId;
                    B.topologicalId = topologicalIdA;
                    int newBackEdgeCount = ordering.countCostEdges(A) + ordering.countCostEdges(B);

                    if (newBackEdgeCount >= backEdgeCount) { // Revert id change
                        B.topologicalId = A.topologicalId;
                        A.topologicalId = topologicalIdA;
                    } else  { // Swap nodes in ordering
                        ordering.swapNodes(A, B);
                        improvementFound = true;
                    }
                }
            }
        }
    }

    private static void localSearchMove(TopologicalOrdering ordering) {

        boolean improvementFound = true;
        while(improvementFound) {
            improvementFound = false;
            for (Node A : ordering.getNodes()) {
                int backEdgeCount = ordering.countCostEdges(A);

                for(int i = 0; i < ordering.getNodes().size(); i++) {



                    int newBackEdgeCount = ordering.countCostEdges(A);

                    if (newBackEdgeCount < backEdgeCount) { // Swap nodes in ordering

                        improvementFound = true;
                    } else if (newBackEdgeCount < backEdgeCount) { // Revert id change

                    }
                }
            }
        }
    }

    private static void localSearchSwapNeighbors(List<Node> orderedNodes, Graph graph) {

        for(int j = 0; j < graph.getNodeCount(); j++) {
            for(int i = 0; i < graph.getNodeCount() - 1; i++) {
                Node A = orderedNodes.get(i);
                Node B = orderedNodes.get(i + 1);

                int backEdgeCount = countBackEdges(graph, A) + countBackEdges(graph, B);

                int topologicalIdA = A.topologicalId;
                A.topologicalId = B.topologicalId;
                B.topologicalId = topologicalIdA;
                int newBackEdgeCount = countBackEdges(graph, A) + countBackEdges(graph, B);


                if(newBackEdgeCount > backEdgeCount) {
                    B.topologicalId = A.topologicalId;
                    A.topologicalId = topologicalIdA;
                } else {
                    orderedNodes.remove(A);
                    orderedNodes.remove(B);
                    orderedNodes.add(B.topologicalId, B);
                    orderedNodes.add(A.topologicalId, A);
                }
            }
        }
    }

    private static void sortByMaxOutMinIn(Graph graph) {

        List<Node> unorderedNodes = new ArrayList<>(graph.getNodes());

        for(int i = 0; i < graph.getNodeCount(); i++) {

            Node nextNode = unorderedNodes.get(0);
            int maxOutCount = 0;
            int minInCount = Integer.MAX_VALUE;
            for(Node node: unorderedNodes) {
                // Count number of out edges to unordered nodes
                int outCount = 0;
                for(Integer outId: node.getOutIds()) {
                    Node out = graph.getNode(outId);
                    if (out.topologicalId == -1) {
                        outCount++;
                    }
                }
                // Count number of in edges from nodes
                int inCount = 0;
                for(Integer inId: node.getInIds()) {
                    Node in = graph.getNode(inId);
                    if (in.topologicalId == -1) {
                        inCount++;
                    }
                }
                if(outCount > maxOutCount) {
                    maxOutCount = outCount;
                    minInCount = inCount;
                    nextNode = node;
                } else if(outCount == maxOutCount && inCount < minInCount) {
                    minInCount = inCount;
                    nextNode = node;
                }
            }
            nextNode.topologicalId = i;
            unorderedNodes.remove(nextNode);
        }
    }
}
