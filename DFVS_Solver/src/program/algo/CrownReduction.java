package program.algo;

import program.heuristics.Solver;
import program.log.Log;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrownReduction {

    public static class Crown {
        public List<Integer> C;
        public List<Integer> H;

        public Crown(List<Integer> C, List<Integer> H) {
            this.C = C;
            this.H = H;
        }
    }

    public static Crown run(Graph graph, Node node) {

        if(!node.hasOnlyDoubleEdges()) return null;

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
            //Log.debugLog(Solver.instance.NAME, "Improved crown reduction to |C| = " + C.size() + " and |H| = " + H.size());
            //Log.debugLog(Solver.instance.NAME, "C = " + C + " and H = " + H);
            return new Crown(C, H);
        }

        return null;
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
}
