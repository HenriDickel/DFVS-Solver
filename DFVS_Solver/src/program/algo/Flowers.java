package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.*;

public abstract class Flowers {

    private static Graph maxFlowGraph;
    private static int[][] maxFlowGraphMatrix;
    private static int N;

    /**
     * Set Petal Value for each node of the graph
     * @param graph The Graph.
     */
    public static void SetAllPetals(Graph graph){

        Node previousNode = null;
        for(Node node : graph.getNodes()){
            node.petal = GetPetal(graph, node, previousNode);
            node.maxPetal =  node.petal;
            previousNode = node;
        }

    }

    private static int GetPetal(Graph graph, Node u, Node previousU){

        //Only create graph from scratch if there is no graph
        if(previousU == null){

            //Copies
            maxFlowGraph = new Graph();

            //Step 1: Replace u
            for(Node node : graph.getNodes()){
                //Ingoing to node-
                if(node.getOutIds().contains(u.id)){
                    maxFlowGraph.addArc(node.label + "+", u.label + "-");
                }
            }

            //Step 2: Replace each w (that is not u)
            for(Node w : graph.getNodes()){
                if(w == u) continue;

                //Add connection
                maxFlowGraph.addArc(w.label + "-", w.label + "+");

                for(Node node : graph.getNodes()){
                    //Ingoing to node-
                    if(node.getOutIds().contains(w.id)){
                        maxFlowGraph.addArc(node.label + "+", w.label + "-");
                    }
                }
            }

            //Values for algorithm
            N = maxFlowGraph.getNodeCount();
            maxFlowGraphMatrix = convertToMatrix(maxFlowGraph, N);
        }
        else{

            //Change prev u
            Node prev_u_plus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, previousU.label + "+")).findFirst().get();
            Node prev_u_minus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, previousU.label + "-")).findFirst().get();
            int prev_index_u_plus = maxFlowGraph.getNodes().indexOf(prev_u_plus);
            int prev_index_u_minus = maxFlowGraph.getNodes().indexOf(prev_u_minus);
            maxFlowGraphMatrix[prev_index_u_minus][prev_index_u_plus] = 1;

            ////Change new u
            Node u_plus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, u.label + "+")).findFirst().get();
            Node u_minus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, u.label + "-")).findFirst().get();
            int index_u_plus = maxFlowGraph.getNodes().indexOf(u_plus);
            int index_u_minus = maxFlowGraph.getNodes().indexOf(u_minus);
            maxFlowGraphMatrix[index_u_minus][index_u_plus] = 0;

        }

        //Find u
        try{
            //Indexes
            Node u_plus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, u.label + "+")).findFirst().get();
            Node u_minus = maxFlowGraph.getNodes().stream().filter(x -> Objects.equals(x.label, u.label + "-")).findFirst().get();
            int index_u_plus = maxFlowGraph.getNodes().indexOf(u_plus);
            int index_u_minus = maxFlowGraph.getNodes().indexOf(u_minus);

            //Get paths
            return findDisjointPaths(maxFlowGraphMatrix, index_u_plus, index_u_minus);


        }
        catch (NoSuchElementException noSuchElementException){
            return 0;
        }

    }

    private static boolean bfs(int[][] rGraph, int s, int t, int[] parent)
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
                if (!visited[v] &&
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
        return (visited[t]);
    }

    // Returns tne maximum number of edge-disjoint
    // paths from s to t. This function is copy of
    // forFulkerson() discussed at http://goo.gl/wtQ4Ks
    private static int findDisjointPaths(int[][] graph, int s, int t)
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

    static private int[][] convertToMatrix(Graph maxFlowGraph, int N){
        int[][] graph = new int[N][N];

        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                Node node_i = maxFlowGraph.getNodes().get(i);
                Node node_j = maxFlowGraph.getNodes().get(j);
                if(node_i.getOutIds().contains(node_j.id)) graph[i][j] = 1;
            }
        }

        return graph;
    }

    public static List<Node> UsePetalRule(Graph graph, int k) {
        List<Node> haveToBeRemoved = new ArrayList<>();

        Node removeNode = findRemoveNode(graph, k);
        while(removeNode != null){
            haveToBeRemoved.add(removeNode);
            graph.removeNode(removeNode);

            //Get next node
            graph.getNodes().forEach(x -> x.petal--);
            removeNode = findRemoveNode(graph, k);
        }

        return haveToBeRemoved;
    }

    private static Node findRemoveNode(Graph graph, int k){
        Node removeNode = null;

        for(Node node :  graph.getNodes()){
            if(node.petal > k){
                if(removeNode == null || removeNode.petal > node.petal) removeNode = node;
            }
        }
        return removeNode;
    }
}
