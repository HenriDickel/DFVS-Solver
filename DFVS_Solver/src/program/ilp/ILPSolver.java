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


    public static void dfvsSolveInstance(Instance instance, long TIME_OUT) {

        //Set instance & branch count
        Solver.instance = instance;

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        PerformanceTimer.reset();
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
        PerformanceTimer.log(PerformanceTimer.MethodType.PREPROCESSING);
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

        for(Graph subGraph: instance.subGraphs) {

            //Check if there is no cycle
            if(DAG.isDAG(subGraph)) continue;

            //List<Integer> S = new ILPSolverOrdering(subGraph).solve(false, false, true, false);
            List<Integer> S = new ILPSolverOrdering(subGraph).solve(TIME_OUT, false, false, true, false);
            instance.S.addAll(S);
        }
    }
}
