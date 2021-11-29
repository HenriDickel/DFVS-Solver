package program;

import program.algo.*;
import program.log.Log;
import program.model.Instance;
import program.model.Node;
import program.utils.InstanceCreator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.Ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile("", fileName);

            // Solve
            Solver.dfvsSolveInstance(instance);

            // Print solution
            for(Node node : instance.S){
                System.out.println(node.label);
            }

            // Print recursive steps
            System.out.println("#recursive steps: " + instance.recursiveSteps);
        } else {

            //Reset Log
            Log.Clear();
            Log.Ignore = false;

            List<Instance> instances = InstanceCreator.createBenchmarkInstances(null);
            instances.forEach(Solver::dfvsSolveInstance);

            // Solve test instances
            //Instance instance = InstanceCreator.createBFSTest2();
            //List<Node> bfs = BFSShortestCircle.ShortestCircleBFS(instance.subGraphs.get(0));
            //List<Node> bfstest = bfs;

            //List<Instance> instances = InstanceCreator.createSelectedInstances();
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve all test instances
            //List<Instance> instances = InstanceCreator.createTestInstances();
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve test instance
            //Instance instance = InstanceCreator.createSimpleNonDAG1();
            //Solver.dfvsSolveInstance(instance);

            // Solve instance from file
            //Instance instance = InstanceCreator.createFromFile("src/inputs/complex/", "biology-n_49-m_689-p_0.9-14");
            //Instance instance = InstanceCreator.createFromFile("src/inputs/complex/", "chess-n_700");
            //Instance instance = InstanceCreator.createFromFile("src/inputs/synthetic/", "synth-n_100-m_1235-k_20-p_0.2.txt");
            //Solver.dfvsSolveInstance(instance);
        }
    }
}
