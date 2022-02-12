package program.heuristics;

import program.algo.Reduction;
import program.log.Log;
import program.model.*;

import java.util.*;

public abstract class DFASHeuristicSolver {

    public static void solveInstance(Instance instance) {

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // TODO use Tarjan?

        // Log preprocessing
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        Map<Integer, DFASMinusNode> minusNodes = new HashMap<>();
        Map<Integer, DFASPlusNode> plusNodes = new HashMap<>();

        for(Node node: initialGraph.getNodes()) {
            DFASMinusNode minusNode = new DFASMinusNode(node.id);
            DFASPlusNode plusNode = new DFASPlusNode(node.id);
            // Add edge minus -> plus
            minusNode.setOutNode(plusNode);
            plusNode.setInNode(minusNode);
            minusNodes.put(node.id, minusNode);
            plusNodes.put(node.id, plusNode);
        }

        for(Node node: initialGraph.getNodes()) {
            DFASMinusNode minusNode = minusNodes.get(node.id);
            for(Integer inId: node.getInIds()) {
                DFASPlusNode inNode = plusNodes.get(inId);
                // Add edge plus -> minus
                minusNode.addInNode(inNode);
            }
            DFASPlusNode plusNode = plusNodes.get(node.id);
            for(Integer outId: node.getOutIds()) {
                DFASMinusNode outNode = minusNodes.get(outId);
                // Add edge plus -> minus
                plusNode.addOutNode(outNode);
            }
        }

        Log.debugLog(instance.NAME, "Splitted up graph in " + minusNodes.size() + " minus nodes and " + plusNodes.size() + " plus nodes");

        //destroyCycles(instance);
        topologicalOrder(instance, minusNodes, plusNodes);

        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size());
    }

    private static void topologicalOrder(Instance instance, Map<Integer, DFASMinusNode> minusNodes, Map<Integer, DFASPlusNode> plusNodes) {

        List<DFASNode> unorderedNodes = new ArrayList<>();
        unorderedNodes.addAll(minusNodes.values());
        unorderedNodes.addAll(plusNodes.values());
        unorderedNodes.forEach(node -> node.topId = -1);

        // Sort by min in count: 6.91
        //TopologicalOrdering ordering = sortByMinIn(unorderedNodes);

        // Sort by min in (pref plus nodes): 4.34
        //TopologicalOrdering ordering = sortByMinInPlusFirst(unorderedNodes);

        // Sort by min in (pref plus nodes) max out: 1.78
        TopologicalOrdering ordering = sortByMinInMaxOut(unorderedNodes);

        // Sort by max out count: 5.05
        //TopologicalOrdering ordering = sortByMaxOut(unorderedNodes);

        // Sort by max out count: 6.71
        //TopologicalOrdering ordering = sortByMaxOutMinIn(unorderedNodes);

        // Sort by min in count -> max out count + local search: 1.498
        //TopologicalOrdering ordering = sortByMinInMaxOut(subGraph);

        //Log.debugLog(instance.NAME, "Heuristic size after sorting: " + ordering.getS().size());
        //localSearchShift(ordering);
        //localSearchSwap(ordering);
        //localSearchCombined(ordering);
        //Log.debugLog(instance.NAME, "Heuristic size after local search: " + ordering.getS().size());

        // Sort by max out count -> min in count: 5.505
        //sortByMaxOutMinIn(subGraph);

        // Add backward edges to S
        instance.S.addAll(ordering.getS());
    }

    private static TopologicalOrdering sort(Graph graph, Map<Integer, DFASMinusNode> minusNodes, Map<Integer, DFASPlusNode> plusNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();
        List<DFASMinusNode> unorderedMinusNodes = new ArrayList<>(minusNodes.values());
        List<DFASPlusNode> orderedPlusNodes = new ArrayList<>(plusNodes.values());

        for(int i = 0; i < graph.getNodeCount(); i++) {

        }

        return ordering;
    }

    private static TopologicalOrdering sortByMinIn(List<DFASNode> unorderedNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();

        int n = unorderedNodes.size();
        System.out.println("Unordered nodes: " + n);

        for(int i = 0; i < n; i++) {

            DFASNode nextNode = unorderedNodes.get(0);
            int inCountMin = Integer.MAX_VALUE;
            for(DFASNode node: unorderedNodes) {
                // Count number of in edges from nodes
                if(node.getTopologicalInCount() < inCountMin) {
                    inCountMin = node.getTopologicalInCount();
                    nextNode = node;
                }
            }
            nextNode.topId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static TopologicalOrdering sortByMinInPlusFirst(List<DFASNode> unorderedNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();

        int n = unorderedNodes.size();
        System.out.println("Unordered nodes: " + n);

        for(int i = 0; i < n; i++) {

            DFASNode nextNode = unorderedNodes.get(0);
            int inCountMin = Integer.MAX_VALUE;
            for(DFASNode node: unorderedNodes) {
                // Count number of in edges from nodes
                if(node.getTopologicalInCount() < inCountMin) {
                    inCountMin = node.getTopologicalInCount();
                    nextNode = node;
                } else if(node.getTopologicalInCount() == inCountMin && inCountMin == 1) {
                    // prefer plus nodes in this case
                    if(node instanceof  DFASPlusNode) {
                        if(!(nextNode instanceof DFASPlusNode)) {
                            nextNode = node;
                        }
                    }
                }
            }
            nextNode.topId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static TopologicalOrdering sortByMinInMaxOut(List<DFASNode> unorderedNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();

        int n = unorderedNodes.size();
        System.out.println("Unordered nodes: " + n);

        for(int i = 0; i < n; i++) {

            DFASNode nextNode = unorderedNodes.get(0);
            int inCountMin = Integer.MAX_VALUE;
            for(DFASNode node: unorderedNodes) {
                // Count number of in edges from nodes
                if(node.getTopologicalInCount() < inCountMin) {
                    inCountMin = node.getTopologicalInCount();
                    nextNode = node;
                } else if(node.getTopologicalInCount() == inCountMin && inCountMin == 1) {
                    // prefer plus nodes in this case
                    if(node instanceof  DFASPlusNode) {
                        if(!(nextNode instanceof DFASPlusNode) || node.getTopologicalOutCount() > nextNode.getTopologicalOutCount()) {
                            nextNode = node;
                        }
                    }
                }
            }
            nextNode.topId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static TopologicalOrdering sortByMaxOut(List<DFASNode> unorderedNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();

        int n = unorderedNodes.size();
        System.out.println("Unordered nodes: " + n);

        for(int i = 0; i < n; i++) {

            DFASNode nextNode = unorderedNodes.get(0);
            int outCountMax = 0;
            for(DFASNode node: unorderedNodes) {
                // Count number of in edges from nodes
                if(node.getTopologicalOutCount() > outCountMax) {
                    outCountMax = node.getTopologicalOutCount();
                    nextNode = node;
                }
            }
            nextNode.topId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static TopologicalOrdering sortByMaxOutMinIn(List<DFASNode> unorderedNodes) {

        TopologicalOrdering ordering = new TopologicalOrdering();

        int n = unorderedNodes.size();
        System.out.println("Unordered nodes: " + n);

        for(int i = 0; i < n; i++) {

            DFASNode nextNode = unorderedNodes.get(0);
            int outCountMax = 0;
            for(DFASNode node: unorderedNodes) {
                // Count number of in edges from nodes
                if(node.getTopologicalOutCount() > outCountMax) {
                    outCountMax = node.getTopologicalOutCount();
                    nextNode = node;
                } else if(node.getTopologicalOutCount() == outCountMax) {
                    // prefer plus nodes in this case
                    if(node.getTopologicalInCount() < nextNode.getTopologicalInCount()) {
                        nextNode = node;
                    }
                }
            }
            nextNode.topId = i;
            unorderedNodes.remove(nextNode);
            ordering.add(nextNode);
        }
        return ordering;
    }

    private static void localSearchSwap(TopologicalOrdering ordering) {

        int kPred = Integer.MAX_VALUE;
        while(ordering.getS().size() < kPred) {
            kPred = ordering.getS().size();
            for (DFASNode A : ordering.getOrderedNodes()) {
                for(DFASNode out: A.getOutNodes()) {

                    if(out.topId < A.topId) { // swap would be beneficial
                        int cost = A.cost() + out.cost() - 1;
                        int topIdA = A.topId;
                        A.topId = out.topId;
                        out.topId = topIdA;
                        int newCost = A.cost() + out.cost();

                        if (newCost > cost) { // Revert id change
                            out.topId = A.topId;
                            A.topId = topIdA;
                        }
                    }
                }
            }
        }
    }

    private static void localSearchShift(TopologicalOrdering ordering) {

        int kPred = Integer.MAX_VALUE;
        while (ordering.getS().size() < kPred) {
            kPred = ordering.getS().size();
            for (DFASNode A : ordering.getOrderedNodes()) {
                for (DFASNode out : A.getOutNodes()) {

                    if (out.topId < A.topId) { // shift A -> out could be beneficial
                        int cost = A.cost();
                        int topIdA = A.topId;
                        A.topId = out.topId;

                        int newCost = A.cost();

                        if (newCost > cost) { // Revert id change
                            A.topId = topIdA;
                        } else { // Swap nodes and found improvement
                            ordering.moveNode(A);
                        }
                    }
                }
            }
        }
    }

    private static void localSearchCombined(TopologicalOrdering ordering) {

        int kPred = Integer.MAX_VALUE;
        while(ordering.getS().size() < kPred) {
            kPred = ordering.getS().size();
            for (DFASNode A : ordering.getOrderedNodes()) {
                for(DFASNode out: A.getOutNodes()) {

                    if(out.topId < A.topId) { // swap would be beneficial
                        int cost = A.cost() + out.cost() - 1;
                        int topIdA = A.topId;
                        A.topId = out.topId;
                        out.topId = topIdA;
                        int newCost = A.cost() + out.cost();

                        if (newCost > cost) { // Revert id change
                            out.topId = A.topId;
                            A.topId = topIdA;
                        }
                    }
                }
            }

            for (DFASNode A : ordering.getOrderedNodes()) {
                for (DFASNode out : A.getOutNodes()) {

                    if (out.topId < A.topId) { // shift A -> out could be beneficial
                        int cost = A.cost();
                        int topIdA = A.topId;
                        A.topId = out.topId;

                        int newCost = A.cost();

                        if (newCost > cost) { // Revert id change
                            A.topId = topIdA;
                        } else { // Swap nodes and found improvement
                            ordering.moveNode(A);
                        }
                    }
                }
            }
        }
    }
}
