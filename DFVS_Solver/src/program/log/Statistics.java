package program.log;

import program.Graph;
import program.utils.MinMaxK;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Statistics {

    private static final String OVERVIEW_MIN_MAX_K_PATH = "src/program/log/OverviewMinMaxK.csv";

    public static void CreateOverviewMinMaxK(List<Graph> graphs){
        //Create File
        CreateFileAndClear(OVERVIEW_MIN_MAX_K_PATH, "name,n,m,min,optimal,max");

        //Fill File
        try(PrintWriter output = new PrintWriter(new FileWriter(OVERVIEW_MIN_MAX_K_PATH,true)))
        {
            for(Graph graph : graphs){
                String name = graph.name;
                int n = graph.getActiveNodes().size();
                int m = graph.getActiveNodes().stream().mapToInt(node -> node.getOutNeighbours().size()).sum();
                int min = MinMaxK.minK(graph);
                int optimal = MinMaxK.optimalK(graph);
                int max = MinMaxK.maxK(graph);
                output.println(name + "," + n + "," + m + "," + min + "," + optimal + "," + max);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void CreateFileAndClear(String path, String header){
        //Create File
        try {
            boolean created = new File(path).createNewFile();
            new PrintWriter(path).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Header
        try(PrintWriter output = new PrintWriter(new FileWriter(path,true)))
        {
            output.println(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
