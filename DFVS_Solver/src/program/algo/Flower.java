package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.*;

public abstract class Flower {

    private  static Graph maxFlowGraph;
    private static int N;

    public static void SetAllPetals(Graph graph){
        for(Node node : graph.getActiveNodes()){
            node.petal = GetPetal(graph, node);
        }
    }

    private static int GetPetal(Graph graph, Node u){

        //Copies
        maxFlowGraph = new Graph();

        //Step 1: Replace u
        for(Node node : graph.getActiveNodes()){
            //Ingoing to node-
            if(node.getOutNeighbors().contains(u)){
                maxFlowGraph.addArc(node.label + "+", u.label + "-");
            }
        }

        //Step 2: Replace each w (that is not u)
        for(Node w : graph.getActiveNodes()){
            if(w == u) continue;

            //Add connection
            maxFlowGraph.addArc(w.label + "-", w.label + "+");

            for(Node node : graph.getActiveNodes()){
                //Ingoing to node-
                if(node.getOutNeighbors().contains(w)){
                    maxFlowGraph.addArc(node.label + "+", w.label + "-");
                }
            }
        }

        //Set N
        N = maxFlowGraph.getActiveNodes().size();

        //Find u
        try{
            Node u_plus = maxFlowGraph.getActiveNodes().stream().filter(x -> Objects.equals(x.label, u.label + "+")).findFirst().get();
            Node u_minus = maxFlowGraph.getActiveNodes().stream().filter(x -> Objects.equals(x.label, u.label + "-")).findFirst().get();

            //Get paths
            return findDisjointPaths(convertToMatrix(), maxFlowGraph.getActiveNodes().indexOf(u_plus), maxFlowGraph.getActiveNodes().indexOf(u_minus));

        }
        catch (NoSuchElementException noSuchElementException){
            return 0;
        }


    }

    private static boolean bfs(int rGraph[][], int s, int t, int parent[])
    {
        // Create a visited array and
        // mark all vertices as not visited
        boolean []visited = new boolean[N];


        // Create a queue, enqueue source vertex and
        // mark source vertex as visited
        Queue <Integer> q = new LinkedList<>();
        q.add(s);
        visited[s] = true;
        parent[s] = -1;

        // Standard BFS Loop
        while (!q.isEmpty())
        {
            int u = q.peek();
            q.remove();

            for (int v = 0; v < N; v++)
            {
                if (visited[v] == false &&
                        rGraph[u][v] > 0)
                {
                    q.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }

        // If we reached sink in BFS
        // starting from source, then
        // return true, else false
        return (visited[t] == true);
    }

    // Returns tne maximum number of edge-disjoint
    // paths from s to t. This function is copy of
    // forFulkerson() discussed at http://goo.gl/wtQ4Ks
    private static int findDisjointPaths(int graph[][], int s, int t)
    {
        int u, v;

        // Residual graph where rGraph[i][j] indicates
        // residual capacity of edge from i to j (if there
        // is an edge. If rGraph[i][j] is 0, then there is not)
        int [][]rGraph = new int[N][N];
        for (u = 0; u < N; u++)
            for (v = 0; v < N; v++)
                rGraph[u][v] = graph[u][v];

        // This array is filled by BFS and to store path
        int[] parent = new int[N];

        int max_flow = 0; // There is no flow initially

        // Augment the flow while there is path
        // from source to sink
        while (bfs(rGraph, s, t, parent))
        {
            // Find minimum residual capacity of the edges
            // along the path filled by BFS. Or we can say
            // find the maximum flow through the path found.
            int path_flow = Integer.MAX_VALUE;

            for (v = t; v != s; v = parent[v])
            {
                u = parent[v];
                path_flow = Math.min(path_flow, rGraph[u][v]);
            }

            // update residual capacities of the edges and
            // reverse edges along the path
            for (v = t; v != s; v = parent[v])
            {
                u = parent[v];
                rGraph[u][v] -= path_flow;
                rGraph[v][u] += path_flow;
            }

            // Add path flow to overall flow
            max_flow += path_flow;
        }

        // Return the overall flow (max_flow is equal to
        // maximum number of edge-disjoint paths)
        return max_flow;
    }

    static private int[][] convertToMatrix(){
        int[][] graph = new int[N][N];

        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                Node node_i = maxFlowGraph.nodes.get(i);
                Node node_j = maxFlowGraph.nodes.get(j);
                if(node_i.getOutNeighbors().contains(node_j)) graph[i][j] = 1;
            }
        }

        return graph;
    }



}
