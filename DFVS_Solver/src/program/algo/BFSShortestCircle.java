package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BFSShortestCircle {

    public static Cycle ShortestCircleBFS(Graph g){

        //Result
        Cycle shortestCircle = BFSCircle(g.getActiveNodes().get(0));

        //Find the shortest circle for each node
        for(Node node : g.getActiveNodes()){
            //Get the shortest circle starting at current node
            Cycle circle = BFSCircle(node);

            //Replace the shortest if shorter
            if(circle != null) {
                if(shortestCircle == null || circle.getNodes().size() < shortestCircle.getNodes().size()) {
                    shortestCircle = circle;
                }
            }
            //Reset fields
            g.resetBFS();
        }

        //Return shortest circle or empty list
        return shortestCircle == null ? new Cycle(null) : shortestCircle;
    }

    private static Cycle BFSCircle(Node root){
        List<Node> Q = new LinkedList<>();
        root.explored = true;
        Q.add(root);
        while(!Q.isEmpty()){
            //Dequeue
            Node v = Q.remove(0);

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

    private static Cycle parentPath(Node node){
        //Circle Path
        Cycle path = new Cycle(node);

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
