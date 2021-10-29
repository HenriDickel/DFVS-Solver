package program;

import program.log.Log;
import program.utils.OptimalK;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

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

            //Reset Log
            Log.Clear();
            Log.Ignore = false;

            //Test Graphs
            List<Graph> graphs = TestCreator.createSyntheticGraphs();

            //Solve
            graphs.forEach(Solver::dfvsSolveSubGraphs);
        }

    }

}
