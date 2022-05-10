package program.algo;

import program.heuristics.Solver;
import program.log.Log;
import program.model.AmbiguousResult;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Reduction {

    public static List<Integer> applyRules(Graph graph, boolean isInitial) {

        //Set updated
        if(isInitial) graph.setAllNodesUpdated();

        //Result
        List<Integer> reduceS = new ArrayList<>();

        boolean crownFound = true;
        crownloop:
        while (crownFound) {
            crownFound = false;

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

                        // Check for double chain remove a <-> node <-> other <-> b => a <-> b and S+1
                        if (isInitial && !node.hasSelfEdge() && node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {

                            Node a = graph.getNode(node.getOutIds().get(0));
                            Node b = graph.getNode(node.getOutIds().get(1));

                            if (a.hasOnlyDoubleEdges() && b.hasOnlyDoubleEdges() && !a.hasSelfEdge() && !b.hasSelfEdge()) {

                                //Log.debugLog(Solver.instance.NAME, "-----------------------Double edge rule: node = " + node.id + ", a = "
                                //        + a.id + " (" + a.getOutIds() + "), b =" +
                                //        " " + b.id + " (" + b.getOutIds() + ")");

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
                                Solver.instance.ambigousS.add(new AmbiguousResult(node.id, b.id, a.id));
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
                                // Set all out nodes to updated (on out node the OP rule could be used now)
                                graph.setAllNodesUpdated(); // TODO dont set the nodes necessary to updated

                            }
                            if (outgoing.size() == 0) {
                                ingoing.forEach(x -> graph.removeEdge(x, node.id));
                                node.updated = true;
                                // Set all out nodes to updated (on out node the OP rule could be used now)
                                graph.setAllNodesUpdated();
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

                        // Quad
                        if (node.hasOnlyDoubleEdges() && node.getOutIdCount() == 2) {
                            Node a = graph.getNode(node.getOutIds().get(0));
                            Node b = graph.getNode(node.getOutIds().get(1));

                            if (a.hasOnlyDoubleEdges() && b.hasOnlyDoubleEdges()) {
                                for (Integer outId : a.getOutIds()) {
                                    if (b.getOutIds().contains(outId) && !outId.equals(node.id)) {

                                        Node c = graph.getNode(outId);

                                        if (c.hasOnlyDoubleEdges() && c.getOutIdCount() == 2) {
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
        /*
                            // Pentagon
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
                            }*/
                    }
                }
            }
            // TODO check or remove the crown reduction part
            /*for(Node node: graph.getNodes()) {
                CrownReduction.Crown crown = CrownReduction.run(graph, node);
                if (crown != null) {
                    for (Integer nodeId : crown.C) {
                        graph.removeNode(nodeId);
                    }
                    for (Integer nodeId : crown.H) {
                        graph.removeNode(nodeId);
                        reduceS.add(nodeId);
                    }
                    crownFound = true;
                    continue crownloop;
                }
            }*/
        }
        return reduceS;
    }

    public static void checkCrown(Graph graph) {

        for(Node node: graph.getNodes()) {

            if(!node.hasOnlyDoubleEdges()) continue;

            List<Integer> C = new ArrayList<>();

            // Initialize crown C with node
            C.add(node.id);

            // Initialize H with neighbors of node
            List<Integer> H = new ArrayList<>(node.getOutIds());
            //Log.debugLog(Solver.instance.NAME, "Initialized crown reduction with |C| = " + C.size() + " and |H| = " + H.size() + " for node " + node);

            int prevCSize = 0;
            while(prevCSize < C.size()) {
                prevCSize = C.size();
                // Look for possible crown nodes in H neighbors
                for(Integer hId: H) {
                    Node hNode = graph.getNode(hId);
                    for(int neighborId: hNode.getOutIds()) {
                        Node neighbor = graph.getNode(neighborId);
                        if(!neighbor.hasOnlyDoubleEdges()) continue;
                        if(H.contains(neighborId) || C.contains(neighborId)) continue;

                        if(H.containsAll(neighbor.getOutIds())) {
                            C.add(neighborId);
                        }
                    }
                }
                increaseH(graph, C, H);
                //System.out.println("End of iteration: |H| = " + H.size());
            }


            if(C.size() >= H.size()) {
                Log.debugLog(Solver.instance.NAME, "Improved crown reduction to |C| = " + C.size() + " and |H| = " + H.size());
                Log.debugLog(Solver.instance.NAME, "C = " + C + " and H = " + H);
            }
        }
    }

    private static void increaseH(Graph graph, List<Integer> C, List<Integer> H) {
        // Look for nodes to add one to C and one to H
        for(Node node: graph.getNodes()) {
            if(!node.hasOnlyDoubleEdges()) continue;
            if(H.contains(node.id) || C.contains(node.id)) continue;

            List<Integer> notContainedInH = node.getOutIds().stream().filter(e -> !H.contains(e)).collect(Collectors.toList());
            if(notContainedInH.size() == 1 && !C.contains(notContainedInH.get(0))) {
                H.add(notContainedInH.get(0));
                C.add(node.id);
            }
        }
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
