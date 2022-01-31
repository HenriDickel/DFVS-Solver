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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class DFVSHeuristicSolver {
    public static void solveInstance(Instance instance) {

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Apply rules on each sub graph
        for(Graph subGraph: instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }

        // Log preprocessing
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        // 5.1 remove nodes min(max(in, out))
        removeNodes(instance);
        // 5.2 destroy cycles
        //destroyCycles(instance);

        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size(), false);
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

    private static void destroyCycles(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while(!DAG.isDAGFast(subGraph)) {
                Cycle cycle = FullBFS.findShortestCycle(subGraph);

                int removeId = -1;
                int inOutMax = 0;
                for(Node node: cycle.getNodes()) {
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
