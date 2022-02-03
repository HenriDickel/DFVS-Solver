package program;

import gurobi.GRBException;
import program.algo.*;
import program.heuristics.Heuristics;
import program.ilp.ILPSolver;
import program.log.Log;
import program.model.*;
import program.utils.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final long TIME_OUT = 90;

    public static void main(String[] args) throws GRBException {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName, -1));

            // Solve
            ILPSolver.dfvsSolveInstance(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }

            // Print recursive steps
            System.out.println("#recursive steps: " + instance.recursiveSteps);
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();

            //Heuristics.testQuality(files);
            files.forEach(Main::run);
        }
    }

    private static void run(GraphFile file) {

        PerformanceTimer.reset();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(() -> {
            long startTime = System.currentTimeMillis();
            PerformanceTimer.start();
            Instance instance = InstanceCreator.createFromFile(file);
            PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);
            Solver.dfvsSolveInstance(instance);
            return (System.currentTimeMillis() - startTime);
        });

        try {
            long millis = future.get(TIME_OUT, TimeUnit.SECONDS);
            Instance instance = Solver.instance;

            // Verify
            instance.solvedK = instance.S.size();
            boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

            // Log results
            PerformanceTimer.printResult();
            Log.mainLog(Solver.instance, millis, PerformanceTimer.getPackingMillis(), verified);
            Color color = verified ? Color.WHITE : Color.RED;
            Log.debugLog(Solver.instance.NAME, "Found solution in " + millis + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
        } catch (TimeoutException e) {
            future.cancel(true);
            Instance instance = Solver.instance;
            instance.solvedK = instance.S.size() + Solver.currentK;

            // Log results
            long millis = TIME_OUT * 1000;
            Log.debugLogAdd("", true);
            PerformanceTimer.printResult();
            Log.mainLog(instance, millis, PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Found no solution in " + millis + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
    }
}
