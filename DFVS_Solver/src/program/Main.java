package program;

import program.ilp.ILPSolver;
import program.model.Instance;
import program.utils.InstanceCreator;
import program.utils.Timer;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Load scip library
            System.loadLibrary("jscip");

            // Start timer
            Timer.start(Integer.MAX_VALUE);

            // Create instance
            Instance instance = InstanceCreator.createPaceInstanceFromSystemIn();

            // Solve
            ILPSolver.solve(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }
        }

    }

}
