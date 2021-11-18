package program;

import program.algo.BFSShortestCircle;
import program.algo.Solver;
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
        } else {

            //Reset Log
            Log.Clear();
            Log.Ignore = false;

            // Solve test instances
            //Instance instance = InstanceCreator.createBFSTest2();
            //List<Node> bfs = BFSShortestCircle.ShortestCircleBFS(instance.subGraphs.get(0));
            //List<Node> bfstest = bfs;

            //List<Instance> instances = InstanceCreator.createComplexInstances();
            //instances.addAll(InstanceCreator.createComplexInstances());
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve own test instances

            //List<Instance> instances = InstanceCreator.createTestInstances();
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve instance from file

            Instance instance = InstanceCreator.createFromFile("src/inputs/complex/", "biology-n_12-m_33-p_0.9-15");
            Solver.dfvsSolveInstance(instance);
            System.out.println(instance.S);

        }

    }
}
