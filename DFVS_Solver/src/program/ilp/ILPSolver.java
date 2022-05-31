package program.ilp;

import program.algo.DAG;
import program.algo.Preprocessing;
import program.algo.Reduction;
import program.model.Graph;
import program.model.Instance;
import program.utils.Timer;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ILPSolver {

    public static Instance instance;

    public static void solve(Instance instance) {

        //Set instance & branch count
        ILPSolver.instance = instance;

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);

        // Apply rules on each sub graph
        for(Graph subGraph: instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }

        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();

        // Run for all sub graphs
        for (Graph subGraph : instance.subGraphs) {

            //Check if there is no cycle
            if(DAG.isDAGFast(subGraph)) continue;

            List<Integer> S = ILPSolverScip.solve(subGraph, Timer.getSecondsLeft());
            instance.S.addAll(S);
        }

        // Add nodes that were ambiguous to result
        instance.addAmbiguousResult();
    }
}

