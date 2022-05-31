package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.*;

public abstract class Heuristic {

    public static List<Integer> GraphTimerFast(Graph subGraph, long timeLimit, float precision, int lowerBound) {

        long endMillis = System.currentTimeMillis() + timeLimit;

        List<Integer> bestS = null;
        while(System.currentTimeMillis() <= endMillis){
            List<Integer> S = GraphTimerRecFast(subGraph, new ArrayList<>(), precision);

            if(S.size() == lowerBound) return S;
            if(bestS == null || S.size() < bestS.size()) {
                bestS = S;
            }

        }
        return bestS;
    }

    public static List<Integer> GraphTimerRecFast(Graph graph, List<Integer> solution, float precision) {

        //Check if DAG
        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> cycles = FullBFS.findMultipleShortestCycles(graph, false);

        //Copy
        Graph copyGraph = graph.copy();
        List<Integer> copySolution = new ArrayList<>(solution);

        //Random
        Random random = new Random();

        //Random
        for(int i = 0; i <= (float) cycles.size() * (1f - precision); i++){

            //Random cycle
            Cycle randomCycle = cycles.get(random.nextInt(cycles.size()));

            //Get best node
            Node node = Collections.max(randomCycle.getNodes(), Comparator.comparing(Node::getMinInOut));

            //Don't delete twice
            if(copySolution.contains(node.id)) continue;

            //Remove node
            copyGraph.removeNode(node.id);
            copySolution.add(node.id);

            //Reduction
            List<Integer> reduceS = Reduction.applyRules(copyGraph, false);
            copySolution.addAll(reduceS);

            // Remove destroyed cycles
            List<Cycle> remove = new ArrayList<>();
            for(Cycle cycle: cycles) {
                if(cycle.contains(node)) {
                    remove.add(cycle);
                } else {
                    for(Node cycleNode: cycle.getNodes()) {
                        if(reduceS.contains(cycleNode.id)) {
                            remove.add(cycle);
                            break;
                        }
                    }
                }
            }
            for (Cycle cycle : remove) {
                cycles.remove(cycle);
            }

            if(cycles.isEmpty()) break;
        }

        //Do again
        return GraphTimerRecFast(copyGraph, copySolution, precision);
    }
}
