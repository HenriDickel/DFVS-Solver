package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleSolver {

    private static List<Integer> dfvsBranch(Graph graph, int k) {

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if (k <= 0) {
            if (DAG.isDAGFast(graph)) {
                return new ArrayList<>();
            }
            else return null;
        }

        Cycle cycle = LightBFS.run(graph);

        List<Integer> forbiddenIds = new ArrayList<>();
        for (Node node: cycle.getNodes()) {
            Graph copy = graph.copy();
            copy.removeNode(node.id);
            copy.removeForbiddenNodes(forbiddenIds);
            //List<Integer> reduceS = Reduction.applyRules(copy, false);
            //int nextK = k - 1 - reduceS.size();
            //if(nextK < 0) continue;

            // Recursive call
            List<Integer> S = dfvsBranch(copy, k - 1);
            if (S != null) {
                S.add(node.id);
                return S;
            }
            forbiddenIds.add(node.id);
        }
        return null;
    }

    public static List<Integer> dfvsSolve(Graph initialGraph) {

        int k = 0;
        List<Integer> S = null;
        while (S == null) {
            S = dfvsBranch(initialGraph, k);
            k++;
        }
        return S;
    }
}
