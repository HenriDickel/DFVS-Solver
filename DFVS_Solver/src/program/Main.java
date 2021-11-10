package program;

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

            List<Instance> instances = InstanceCreator.createSyntheticInstances();
            instances.addAll(InstanceCreator.createComplexInstances());
            instances.forEach(Solver::dfvsSolveInstance);

            // Solve own test instances

            //List<Instance> instances = InstanceCreator.createTestInstances();
            //instances.forEach(Solver::dfvsSolveInstance);

            // Solve instance from file

            //Graph graph = TestCreator.createFromFile("src/inputs/complex/", "GD-n_73-m_96.mtx");
            //List<Node> S = Solver.dfvsSolveSubGraphs(graph);
            //S.forEach(s -> System.out.println(s.label));

        }

    }
}
