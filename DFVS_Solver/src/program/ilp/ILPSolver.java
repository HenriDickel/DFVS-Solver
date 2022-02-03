package program.ilp;

import program.algo.DAG;
import program.algo.Preprocessing;
import program.algo.Reduction;
import program.algo.Solver;
import program.log.Log;
import program.model.Graph;
import program.model.Instance;
import program.utils.Color;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ILPSolver {


    public static void dfvsSolveInstance(Instance instance) {

        //Set instance & branch count
        Solver.instance = instance;

        // Start Timer
        int timeLimit = 90;
        Timer.start(90);

        PerformanceTimer.reset();
        PerformanceTimer.start();

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
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

        try {
            for(Graph subGraph: instance.subGraphs) {

                //Check if there is no cycle
                if(DAG.isDAG(subGraph)) continue;

                List<Integer> S = new ILPSolverLazyCycles(subGraph, Timer.getSecondsLeft()).solve(instance, true, true);
                instance.S.addAll(S);
            }
        } catch (TimeoutException e) {
            long millis = timeLimit * 1000;
            Log.ilpLog(instance, millis, false);
            Log.detailLog(instance);
            PerformanceTimer.printILPResult();
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(millis), Color.RED);
            return;
        }

        // Stop Timer
        long millis = Timer.getMillis();

        // Log
        instance.solvedK = instance.S.size();
        boolean verified = millis < timeLimit * 1000;
        if(millis > timeLimit * 1000) millis = timeLimit * 1000;
        Log.ilpLog(instance, millis, verified);
        PerformanceTimer.printILPResult();
        Color color = verified ? Color.WHITE : Color.RED;
        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(millis), color);
    }
}
