import java.util.ArrayList;
import java.util.List;

public abstract class GraphUtils {

    private static int index;
    private static int cycleStartIndex;

    public static List<Node> findFirstCycle(Graph graph) {

        index = 0;
        for(Node node: graph.getActiveNodes()) {
            graph.unvisitAllNodes();
            if(node.visited == -1) {
                List<Node> cycle = visitNode(node);
                if(cycle != null) return cycle;
            }
        }
        return null;
    }

    private static List<Node> visitNode(Node node) {

        if(node.visited != -1) {
            List<Node> pathToStart = new ArrayList<>();
            pathToStart.add(node);
            cycleStartIndex = node.visited;
            return pathToStart;
        } else {
            node.visited = index;
            index++;

            for (Node out : node.getOutNeighbours()) {
                List<Node> pathToStart = visitNode(out);
                if(pathToStart != null) {
                    if(node.visited > cycleStartIndex) pathToStart.add(node);
                    node.visited = -1;
                    return pathToStart;
                }
            }

            node.visited = -1;
            return null;
        }
    }
}
