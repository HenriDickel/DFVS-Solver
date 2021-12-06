package program;

import program.algo.*;
import program.log.Log;
import program.model.Instance;
import program.utils.InstanceCreator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile("", fileName);

            // Solve
            Solver.dfvsSolveInstance(instance);

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

            //List<Instance> instances = InstanceCreator.createBenchmarkInstances(null);
            //instances.forEach(Solver::dfvsSolveInstance);

            List<Instance> instances = InstanceCreator.createSelectedInstances();
            instances.forEach(Solver::dfvsSolveInstance);

            // Solve test instances
            //Instance instance = InstanceCreator.createBFSTest2();
            //List<Node> bfs = BFSShortestCircle.ShortestCircleBFS(instance.subGraphs.get(0));
            //List<Node> bfstest = bfs;

            // Solve all test instances
            //List<Instance> instances = InstanceCreator.createTestInstances();
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve test instance
            //Instance instance = InstanceCreator.createSimpleNonDAG1();
            //Solver.dfvsSolveInstance(instance);

            // Solve instance from file
            //Instance instance = InstanceCreator.createFromFile("src/inputs/complex/", "biology-n_49-m_689-p_0.9-14");
            //Instance instance = InstanceCreator.createFromFile("src/inputs/dataset_2/complex/", "oz");
            //Instance instance = InstanceCreator.createFromFile("src/inputs/dataset_2/synthetic/", "synth-n_120-m_921-k_25-p_0.1.txt");
            //Solver.dfvsSolveInstance(instance);
            //System.out.println(instance.S);
        }
    }
}
