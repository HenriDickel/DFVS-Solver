package program.heuristics;

import program.log.Log;
import program.model.GraphFile;
import program.model.Instance;
import program.utils.Dataset;
import program.utils.InstanceCreator;

import java.util.List;

public abstract class Heuristics {

    public static void testQuality(List<GraphFile> files) {
        // Initialize aggregation vars
        int approxKAgg = 0;
        int optimalKAgg = 0;
        float qualityAgg = 0;
        long millisAgg = 0;
        int count = 0;
        for(GraphFile file: files) {
            int optimalK = InstanceCreator.readOptimalKFromFile(InstanceCreator.getSolutionPath(Dataset.DATASET_3), file.name);
            if(optimalK <= 0) continue;
            Instance instance = InstanceCreator.createFromFile(file, optimalK);

            long startMillis = System.currentTimeMillis();

            // Solve instance with heuristic
            //DFASHeuristicSolver.solveInstance(instance);
            DFVSHeuristicSolver.solveInstance(instance);
            int approxK = instance.S.size();

            // Update aggregation vars
            approxKAgg += approxK;
            optimalKAgg += instance.OPTIMAL_K;
            qualityAgg += (float) approxK / instance.OPTIMAL_K;
            long millis = System.currentTimeMillis() - startMillis;
            millisAgg += millis;
            count++;

            // Log
            Log.debugLog(instance.NAME, "Heuristics: " + approxK + " / " + instance.OPTIMAL_K + " (in " + millis + " ms)");
            Log.heuristicLog(instance, approxK, millis);

        }
        float quality = qualityAgg / count;
        float averageMillis = (float) millisAgg / count;
        Log.debugLog("Overall (" + count + ")", "Average heuristics quality per instance: " + quality  + " (in " + averageMillis + " ms)");
        float overallSolvedK = (float) approxKAgg / optimalKAgg;
        Log.debugLog("Overall (" + count + ")", "Overall heuristics quality: " + overallSolvedK + " (in " + millisAgg + " ms)");
    }
}
