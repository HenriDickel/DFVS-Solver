package program.ilp;

import program.algo.DAG;
import program.algo.Preprocessing;
import program.algo.Reduction;
import program.algo.Solver;
import program.log.Log;
import program.model.Graph;
import program.model.Instance;
import program.utils.PerformanceTimer;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ILPSolver {


    public static void solve(Instance instance) {

        //Set instance & branch count
        Solver.instance = instance;

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        PerformanceTimer.start();
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

        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);
        PerformanceTimer.log(PerformanceTimer.MethodType.PREPROCESSING);

        // Run for all sub graphs
        for (Graph subGraph : instance.subGraphs) {

            //Check if there is no cycle
            if(DAG.isDAGFast(subGraph)) continue;

            float nodePercentage = (float) subGraph.getNodeCount() / instance.getCurrentN();
            System.out.println(subGraph.getNodeCount() + "/" +  instance.getCurrentN());
            // TODO ILP
            System.out.println("Call ILP");
            List<Integer> S = new ILPSolverVertexCover(subGraph, 90).solve(instance);
            instance.S.addAll(S);
        }

        // Add nodes that were ambiguous to result
        instance.addAmbiguousResult();
    }
}

