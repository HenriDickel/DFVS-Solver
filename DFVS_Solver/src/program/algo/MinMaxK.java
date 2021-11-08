package program.algo;

import program.model.Graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class MinMaxK {

    public static int minK(Graph graph){

        int n = graph.getActiveNodes().size();
        int m = graph.getActiveNodes().stream().mapToInt(node -> node.getOutNeighbours().size()).sum();

        List<Integer> smallestKList = new ArrayList<>();

        for(int i = 0; i <= n * (n-1) / 2; i++){
            smallestKList.add(0);
        }

        for(int j = 1; j < n; j++){
            for(int i = 0; i < (n-j); i++){
                smallestKList.add(j);
            }
        }

        return smallestKList.get(m);
    }

    public static int maxK(Graph graph){

        int n = graph.getActiveNodes().size();
        int m = graph.getActiveNodes().stream().mapToInt(node -> node.getOutNeighbours().size()).sum();

        if(m == n * (n-1)) return n - 1;

        return Math.min(n - 2, m / 2);
    }

    public static int optimalK(Graph graph){
        try{
            Scanner scan = new Scanner(new File("src/inputs/optimal_solution_sizes.txt"));
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.startsWith(graph.name)){
                    String optimalK = line.split("     ")[1];
                    return Integer.parseInt(optimalK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Not in optimal solutions
        return -1;
    }

}
