package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Reduction {

    public static List<Integer> applyRules(Graph graph, boolean updateAll) {

        //Set updated
        if(updateAll) graph.setAllNodesUpdated();

        //Result
        List<Integer> reduceS = new ArrayList<>();

        //Diamond
        //reduceS.addAll(diamondCompleteRemove(graph));

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

                } else { // Other rules

                    // Fully connected triangle
                    if (node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {
                        Node a = graph.getNode(node.getOutIds().get(0));
                        Node b = graph.getNode(node.getOutIds().get(1));

                        if(a.getOutIds().contains(b.id) && b.getOutIds().contains(a.id)) {
                            graph.removeNode(node.id);
                            graph.removeNode(a.id);
                            graph.removeNode(b.id);
                            reduceS.add(a.id);
                            reduceS.add(b.id);
                            break;
                        }
                    }

                    // Fully connected quad
                    if (node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {
                        Node a = graph.getNode(node.getOutIds().get(0));
                        Node b = graph.getNode(node.getOutIds().get(1));

                        if(a.hasOnlyDoubleEdges() && b.hasOnlyDoubleEdges()) {
                            for(Integer outId: a.getOutIds()) {
                                if(b.getOutIds().contains(outId) && !outId.equals(node.id)) {

                                    Node c = graph.getNode(outId);

                                    if(c.hasOnlyDoubleEdges() && c.getOutIdCount() == 2) {
                                        graph.removeNode(node.id);
                                        graph.removeNode(a.id);
                                        graph.removeNode(b.id);
                                        graph.removeNode(outId);
                                        reduceS.add(a.id);
                                        reduceS.add(b.id);
                                        break mainloop;
                                    }
                                }
                            }
                        }
                    }

                    // Fully connected pentagon
                    if (node.hasOnlyDoubleEdges() && node.getOutIdCount() >= 2) {
                        for(Integer aId: node.getOutIds()) {
                            for(Integer bId: node.getOutIds()) {

                                if(aId.equals(bId)) continue;

                                Node a = graph.getNode(aId);
                                Node b = graph.getNode(bId);

                                if(a.hasOnlyDoubleEdges() && !a.hasSelfEdge() && a.getOutIdCount() == 2 &&
                                        b.hasOnlyDoubleEdges() && !b.hasSelfEdge() && b.getOutIdCount() == 2) { // TODO can be verallgemeinert

                                    Integer cId = (b.getOutIds().get(0).equals(node.id)) ? b.getOutIds().get(1) : b.getOutIds().get(0);
                                    Integer dId = (a.getOutIds().get(0).equals(node.id)) ? a.getOutIds().get(1) : a.getOutIds().get(0);
                                    Node c = graph.getNode(cId);
                                    Node d = graph.getNode(dId);

                                    if(!cId.equals(dId) && c.getOutIds().contains(dId) && d.getOutIds().contains(cId)) {

                                        graph.removeNode(node.id);
                                        graph.removeNode(a.id);
                                        graph.removeNode(b.id);
                                        graph.removeNode(c.id);
                                        graph.removeNode(d.id);
                                        reduceS.add(node.id);
                                        reduceS.add(c.id);
                                        reduceS.add(d.id);
                                        break mainloop;
                                    }
                                }
                            }
                        }
                    }

                    // Remove trivial edges on double edge node
                    if (node.getOutIds().stream().anyMatch(x -> node.getInIds().contains(x)) && node.getOutIdCount() != node.getInIdCount()) {
                        List<Integer> ingoing = node.getInIds().stream().filter(x -> !node.getOutIds().contains(x)).collect(Collectors.toList());
                        List<Integer> outgoing = node.getOutIds().stream().filter(x -> !node.getInIds().contains(x)).collect(Collectors.toList());
                        if (ingoing.size() == 0) {
                            outgoing.forEach(x -> graph.removeEdge(node.id, x));
                            node.updated = true;
                        }
                        if (outgoing.size() == 0) {
                            ingoing.forEach(x -> graph.removeEdge(x, node.id));
                            node.updated = true;
                        }
                    }
                }
            }
        }
        return reduceS;
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

    private static List<Integer> diamondCompleteRemove(Graph graph){

        //Result
        List<Integer> result = new ArrayList<>();

        //Get shortest cycles
        HashMap<Integer, List<Integer>> shortestCycles = CycleUndirected.shortestCycles(graph);

        //For all nodes
        for(Node node : graph.getNodes()){
            if(result.contains(node.id)) continue;

            List<Integer> shortestCycle = shortestCycles.get(node.id);
            List<Integer> ruleNodes = applyDiamondRule(graph, shortestCycle);

            result.addAll(ruleNodes);

            for(int removeNode : ruleNodes) graph.removeNode(removeNode);
        }

        //Return result
        return result;

    }

    private static List<Integer> applyDiamondRule(Graph graph, List<Integer> cycleNodes){

        //Result
        List<Integer> result = new ArrayList<>();

        //Check if cycle exists
        if(cycleNodes.size() == 0) return new ArrayList<>();

        //One node can be a 'bridge' to the rest of the graph
        Node exceptionNode = null;

        for(Integer nodeId : cycleNodes){
            Node node = graph.getNode(nodeId);
            if(!node.hasOnlyDoubleEdges()) return new ArrayList<>();

            if(node.getOutIdCount() > 2){
                if(exceptionNode != null) return new ArrayList<>();
                else exceptionNode = node;
            }
        }

        //Create path
        List<Integer> sorted = new ArrayList<>();

        //Start node
        if(exceptionNode == null) sorted.add(cycleNodes.get(0));
        else sorted.add(exceptionNode.id);

        //Sort
        while(sorted.size() < cycleNodes.size()){
            Node last = graph.getNode(sorted.get(sorted.size() - 1));
            for(int connected : last.getOutIds()){
                if(!cycleNodes.contains(connected)) continue;
                if(!sorted.contains(connected)){
                    sorted.add(connected);
                    break;
                }
            }
        }

        //Replaceable
        if(cycleNodes.size() % 2 == 0){
            for(int i = 0; i < cycleNodes.size(); i+=2){
                result.add(sorted.get(i));
                graph.removeNode(sorted.get(i));
            }
        }

        //Replaceable uneven (???)
        if(cycleNodes.size() % 2 == 1){
            for(int i = 0; i < cycleNodes.size(); i+=2){
                result.add(sorted.get(i));
                graph.removeNode(sorted.get(i));
            }
        }

        return result;
    }
}
