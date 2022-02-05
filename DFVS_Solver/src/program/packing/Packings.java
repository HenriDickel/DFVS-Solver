package program.packing;

import program.algo.Reduction;
import program.log.Log;
import program.model.Graph;
import program.model.GraphFile;
import program.model.Instance;
import program.utils.Color;
import program.utils.InstanceCreator;

import java.util.List;

public abstract class Packings {

    public static void testQuality(List<GraphFile> files) {
        // Initialize aggregation vars
        int packingKAgg = 0;
        int optimalKAgg = 0;
        float qualityAgg = 0;
        long millisAgg = 0;
        int count = 0;
        for(GraphFile file: files) {
            if(file.optimalK <= 0) continue;
            Instance instance = InstanceCreator.createFromFile(file);
            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);

            long startMillis = System.currentTimeMillis();

            // Solve instance with heuristic
            Graph initialGraph = instance.subGraphs.get(0);
            List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
            PackingManager pm = new PackingManager(initialGraph);
            int packingK = reduceS.size() + pm.size();

            if(packingK > instance.OPTIMAL_K) {
                System.out.println(reduceS);
                System.out.println(pm.getPacking());
                throw new RuntimeException("Packing is invalid! (" + packingK + "/" + instance.OPTIMAL_K + ")");
            }

            // Update aggregation vars
            packingKAgg += packingK;
            optimalKAgg += instance.OPTIMAL_K;
            qualityAgg += (float) packingK / instance.OPTIMAL_K;
            long millis = System.currentTimeMillis() - startMillis;
            millisAgg += millis;
            count++;

            // Log
            Log.debugLog(instance.NAME, "Packing: " + packingK + " / " + instance.OPTIMAL_K + " (in " + millis + " ms)");
            Log.heuristicLog(instance, packingK, millis);

        }
        float quality = qualityAgg / count;
        float averageMillis = (float) millisAgg / count;
        Log.debugLog("Overall (" + count + ")", "Average packing quality per instance: " + quality  + " (in " + averageMillis + " ms)");
        float overallPackingK = (float) packingKAgg / optimalKAgg;
        Log.debugLog("Overall (" + count + ")", "Overall packing quality: " + overallPackingK + " (in " + millisAgg + " ms)");
    }
}
