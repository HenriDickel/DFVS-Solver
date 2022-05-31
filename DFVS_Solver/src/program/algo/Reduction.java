package program.algo;

import program.ilp.ILPSolver;
import program.model.AmbiguousResult;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Reduction {

    public static List<Integer> applyRules(Graph graph, boolean isInitial) {

        //Set updated
        if(isInitial) graph.setAllNodesUpdated();

        //Result
        List<Integer> reduceS = new ArrayList<>();

        List<Integer> updatedNodeIds;
        while (!(updatedNodeIds = graph.getUpdatedNodeIds()).isEmpty()) {
            mainloop:
            for (Integer id : updatedNodeIds) {
                Node node = graph.getNode(id);
                node.updated = false;
                if (node.getOutIdCount() == 0 || node.getInIdCount() == 0) { // trivial vertex
                    graph.removeNode(node.id);
                } else if (node.hasSelfEdge()) { // self loop
                    graph.removeNode(node.id);
                    reduceS.add(node.id);
                } else if (node.getOutIdCount() == 1) { // chain rule (in >>> node -> out)
                    Integer outId = node.getOutIds().get(0);
                    Node out = graph.getNode(outId);
                    for (Integer inId : node.getInIds()) {
                        Node in = graph.getNode(inId);
                        in.addOutId(outId);
                        out.addInId(inId);
                    }
                    graph.removeNode(node.id);
                } else if (node.getInIdCount() == 1) { // chain rule (in -> node >>> out)
                    Integer inId = node.getInIds().get(0);
                    Node in = graph.getNode(inId);
                    for (Integer outId : node.getOutIds()) {
                        Node out = graph.getNode(outId);
                        out.addInId(inId);
                        in.addOutId(outId);
                    }
                    graph.removeNode(node.id);
                } else if (checkSuperset(graph, node)) { // Check for super set remove
                    graph.removeNode(node.id);
                    reduceS.add(node.id);
                } else if (checkFullyConnected(graph, node)) { // Check for fully connected remove
                    for(Integer outId: new ArrayList<>(node.getOutIds())) {
                        graph.removeNode(outId);
                        reduceS.add(outId);
                    }
                    graph.removeNode(node.id);
                    break;
                } else { // Other rules

                    // Check for double chain remove a <-> node <-> other <-> b => a <-> b and S+1
                    if (isInitial && !node.hasSelfEdge() && node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {

                        Node a = graph.getNode(node.getOutIds().get(0));
                        Node b = graph.getNode(node.getOutIds().get(1));

                        if (a.hasOnlyDoubleEdges() && b.hasOnlyDoubleEdges() && !a.hasSelfEdge() && !b.hasSelfEdge()) {

                            for (Integer outId : b.getOutIds()) {
                                if (outId.equals(node.id)) continue;
                                if (a.getOutIds().contains(outId)) continue;

                                Node other = graph.getNode(outId);
                                a.addOutId(outId);
                                other.addInId(a.id);
                                other.addOutId(a.id);
                                a.addInId(other.id);
                                other.updated = true;
                            }

                            // Remove nodes and add to reduceS
                            graph.removeNode(node.id);
                            graph.removeNode(b.id);
                            //reduceS.add(node.id);
                            ILPSolver.instance.ambigousS.add(new AmbiguousResult(node.id, b.id, a.id));
                            a.updated = true;
                            break;
                        }
                    }

                    // Remove trivial edges on double edge node
                    if (node.getOutIds().stream().anyMatch(x -> node.getInIds().contains(x)) && node.getOutIdCount() != node.getInIdCount()) {
                        List<Integer> ingoing = node.getInIds().stream().filter(x -> !node.getOutIds().contains(x)).collect(Collectors.toList());
                        List<Integer> outgoing = node.getOutIds().stream().filter(x -> !node.getInIds().contains(x)).collect(Collectors.toList());
                        if (ingoing.size() == 0) {
                            outgoing.forEach(x -> graph.removeEdge(node.id, x));
                            node.updated = true;
                            graph.setAllNeighborsUpdated(node.id);
                            for(Integer outId: outgoing) {
                                graph.setAllNeighborsUpdated(outId);
                            }

                        }
                        if (outgoing.size() == 0) {
                            ingoing.forEach(x -> graph.removeEdge(x, node.id));
                            node.updated = true;
                            graph.setAllNeighborsUpdated(node.id);
                            for(Integer outId: outgoing) {
                                graph.setAllNeighborsUpdated(outId);
                            }
                        }
                    }

                    // Fully connected triangle
                    if (node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {
                        Node a = graph.getNode(node.getOutIds().get(0));
                        Node b = graph.getNode(node.getOutIds().get(1));

                        if (a.getOutIds().contains(b.id) && b.getOutIds().contains(a.id)) {
                            graph.removeNode(node.id);
                            graph.removeNode(a.id);
                            graph.removeNode(b.id);
                            reduceS.add(a.id);
                            reduceS.add(b.id);
                            break;
                        }
                    }
                }
            }
        }
        return reduceS;
    }

    private static boolean checkFullyConnected(Graph graph, Node node) {
        if(!node.hasOnlyDoubleEdges()) return false;

        List<Integer> otherNeighborIds = new ArrayList<>();
        for(Integer neighborId: node.getOutIds()) {
            Node neighbor = graph.getNode(neighborId);
            if(!neighbor.getFullyConnectedIds().containsAll(otherNeighborIds)) {
                return false;
            } else {
                otherNeighborIds.add(neighborId);
            }
        }
        return true;
    }

    private static boolean checkSuperset(Graph graph, Node node) {

        if(!node.hasOnlyDoubleEdges()) return false;

        for(Integer outId: node.getOutIds()) {

            Node neighbor = graph.getNode(outId);
            if(!neighbor.hasOnlyDoubleEdges()) continue;

            // Skip if neighbor has self loop
            if(neighbor.hasSelfEdge()) continue;

            List<Integer> filteredOutIds = neighbor.getOutIds().stream().filter(e -> !e.equals(node.id)).collect(Collectors.toList());

            if(node.getOutIds().containsAll(filteredOutIds)) return true;
        }
        return false;
    }
}
