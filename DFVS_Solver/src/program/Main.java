package program;

import program.algo.*;
import program.log.Log;
import program.model.GraphFile;
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
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName));

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

            List<GraphFile> files = InstanceCreator.getSyntheticAndComplexFiles(null);
            for(GraphFile file: files) {
                Instance instance = InstanceCreator.createFromFile(file);
                Solver.dfvsSolveInstance(instance);
            }
        }
    }
}
