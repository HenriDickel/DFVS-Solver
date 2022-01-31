package program.heuristics;

import program.algo.DAG;
import program.algo.FullBFS;
import program.algo.Preprocessing;
import program.algo.Reduction;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.utils.PerformanceTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            for(int i = 0; i < subGraph.getNodeCount(); i++) {

                Node next = null;
                int backEdgeCountMin = Integer.MAX_VALUE;
                for(Node node: subGraph.getNodes()) {
                    if(node.topologicalId > -1) continue;

                     // Count number of in edges from the unordered nodes
                    int backEdgeCount = 0;
                    for(Integer inId: node.getInIds()) {
                        Node in = subGraph.getNode(inId);
                        if (in.topologicalId == -1) {
                            backEdgeCount++;
                        }
                    }
                    if(backEdgeCount < backEdgeCountMin) {
                        backEdgeCountMin = backEdgeCount;
                        next = node;
                    }
                }
                next.topologicalId = i;
            }

            // Add backward edges to S
            for(Node node: subGraph.getNodes()) {
                for (Integer outId: node.getOutIds()) {
                    Node out = subGraph.getNode(outId);
                    if(out.topologicalId < node.topologicalId) { // If the out node is earlier in the ordering
                        instance.S.add(out.topologicalId);
                    }
                }
            }
        }

    }

    private static void destroyCycles(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while(!DAG.isDAGFast(subGraph)) {
                Cycle cycle = FullBFS.findShortestCycle(subGraph);
                int removeId = cycle.get(0).id;
                subGraph.removeNode(removeId);
                instance.S.add(removeId);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void removeEdges(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while(!DAG.isDAGFast(subGraph)) {
                int removeId = -1;
                int inOutMax = 0;
                for(Node node: subGraph.getNodes()) {
                    int inOut = Math.min(node.getOutIdCount(), node.getInIdCount());
                    if(inOut > inOutMax) {
                        removeId = node.id;
                        inOutMax = inOut;
                    }
                }
                subGraph.removeNode(removeId);
                instance.S.add(removeId);
            }
        }
    }

    private static void removeNodes(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while(!DAG.isDAGFast(subGraph)) {
                int removeId = -1;
                int inOutMax = 0;
                for(Node node: subGraph.getNodes()) {
                    int inOut = Math.min(node.getOutIdCount(), node.getInIdCount());
                    if(inOut > inOutMax) {
                        removeId = node.id;
                        inOutMax = inOut;
                    }
                }
                subGraph.removeNode(removeId);
                instance.S.add(removeId);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }
}
