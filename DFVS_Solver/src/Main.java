import log.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        //Ignore Log
        Log.Ignore = true;

        //As args
        if(args.length > 0){

            //Path
            String fileName = args[0];

            //Create Graph
            Graph graph = GraphFileManager.CreateFromFile("", fileName);

            //Solve
            List<Node> nodes = Solver.dfvsSolveSubgraphs(graph);

            //Print solution
            for(Node node : nodes){
                System.out.println(node.label);
            }
        }

    }

}
