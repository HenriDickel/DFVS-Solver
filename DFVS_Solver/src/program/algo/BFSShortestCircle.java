package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;

public class BFSShortestCircle {

    public static List<Node> ShortestCircleBFS(Graph g){
        //Result
        List<Node> shortestCircle = BFSCircle(g.getActiveNodes().get(0));

        //Find the shortest circle for each node
        for(Node node : g.getActiveNodes()){
            //Get the shortest circle starting at current node
            List<Node> circle = BFSCircle(node);

            //Replace the shortest if shorter
            if(circle != null && shortestCircle != null && circle.size() < shortestCircle.size()) shortestCircle = circle;

            //Reset fields
            g.resetBFS();
        }

        //Return shortest circle or empty list
        return shortestCircle == null ? new ArrayList<>() : shortestCircle;
    }

    private static List<Node> BFSCircle(Node root){
        List<Node> Q = new ArrayList<>();
        root.explored = true;
        Q.add(root);
        while(!Q.isEmpty()){
            //Dequeue
            Node v = Q.get(Q.size() - 1);
            Q.remove(Q.size() - 1);

            //Check if root is reached
            if(v.getOutNeighbors().contains(root)){
                return parentPath(v);
            }

            //Add neighbors
            for(Node node : v.getUnexploredNeighbors()){
                    node.explored = true;
                    node.parent = v;
                    Q.add(node);
            }
        }

        //No Circle Found
        return null;
    }

    private static List<Node> parentPath(Node node){
        //Circle Path
        List<Node> path = new ArrayList<>(List.of(node));

        //Parent of end node
        Node parent = node.parent;

        //Loop up
        while(parent != null){
            path.add(parent);
            parent = parent.parent;
        }

        //Return path
        return path;
    }


}
