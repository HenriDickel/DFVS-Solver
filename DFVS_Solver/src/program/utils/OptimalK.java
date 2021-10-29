package program.utils;

import program.Graph;
import program.Node;

import java.util.ArrayList;
import java.util.List;

public class OptimalK {

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

        int m = graph.getActiveNodes().stream().mapToInt(node -> node.getOutNeighbours().size()).sum();

        return m / 2;
    }

}
