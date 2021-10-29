package program;

import program.log.Log;
import program.utils.SmallestK;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //As args
        if(args.length > 0){

            //Ignore Log
            Log.Ignore = false;

            //Path
            String fileName = args[0];

            //Create program.Graph
            Graph graph = TestCreator.createFromFile("", fileName);

            //Solve
            List<Node> nodes = Solver.dfvsSolveSubGraphs(graph);

            //Print solution
            for(Node node : nodes){
                System.out.println(node.label);
            }
        } else {

            //Ignore Log
            Log.Clear();
            Log.Ignore = false;

            //Test Graphs
            List<Graph> graphs = TestCreator.createComplexGraphs();

            //Solve
            graphs.forEach(Solver::dfvsSolveSubGraphs);
        }

    }

}
