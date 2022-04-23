package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public class CycleUndirected {

        //Finds the shortest undirected cycle
        @Deprecated
        public static List<Node> shortestCycle(Graph graph, Node startNode)
        {
            List<Node> shortest = new ArrayList<>(graph.getNodes());

            int n = graph.getNodes().stream().map(x -> x.id).max(Integer::compare).get() + 1;

            List<Node>[] paths = new ArrayList[n];

            Vector<Integer>[] gr = new Vector[n];

            for (int i = 0; i < n; i++) gr[i] = new Vector<>();
            for (int i = 0; i < n; i++) paths[i] = new ArrayList<>();

            for(Node node : graph.getNodes()){
                for(int out : node.getOutIds()){
                    gr[node.id].add(out);
                    gr[out].add(node.id);
                }
            }

            // To store length of the shortest cycle
            int ans = Integer.MAX_VALUE;

            int i = startNode.id;

            // Make distance maximum
            int[] dist = new int[n];
            Arrays.fill(dist, (int) 1e9);

            // Take a imaginary parent
            int[] par = new int[n];
            Arrays.fill(par, -1);

            // Distance of source to source is 0
            dist[i] = 0;
            Queue<Integer> q = new LinkedList<>();

            // Push the source element
            q.add(i);

            // Continue until queue is not empty
            while (!q.isEmpty())
            {

                // Take the first element
                int x = q.poll();

                // Traverse for all it's childs
                for (int child : gr[x])
                {
                    // If it is not visited yet
                    if (dist[child] == (int) (1e9))
                    {

                        // Increase distance by 1
                        dist[child] = 1 + dist[x];
                        paths[child] = new ArrayList<>(paths[x]);
                        paths[child].add(graph.getNode(child));

                        // Change parent
                        par[child] = x;

                        // Push into the queue
                        q.add(child);
                    } else if (par[x] != child && par[child] != x)
                        if(ans > dist[x] + dist[child] + 1){
                            ans = dist[x] + dist[child] + 1;

                            List<Node> newPath = new ArrayList<>(paths[x]);
                            newPath.addAll(new ArrayList<>(paths[child]));
                            newPath.add(graph.getNode(i));

                            shortest = new ArrayList<>(newPath);
                        }

                }
            }


            //Return result
            return shortest;
        }

        //Finds the shortest undirected cycles
        public static HashMap<Integer, List<Integer>> shortestCycles(Graph graph)
        {
            //Shortest cycles
            HashMap<Integer, List<Integer>> shortestCycles = new HashMap<>();
            graph.getNodes().forEach(x -> shortestCycles.put(x.id, new ArrayList<>()));

            //Undirected graph
            HashMap<Integer, List<Integer>> gr = new HashMap<>();

            //Initialize graph
            for(Node node : graph.getNodes()){
                for(int out : node.getOutIds()){
                    gr.put(node.id, new ArrayList<>(out));
                    gr.put(out, new ArrayList<>(node.id));
                }
            }

            //To store length of the shortest cycle
            int ans = Integer.MAX_VALUE;

            for(Node node : graph.getNodes()){

                //Current node id
                int i = node.id;

                // Make distance maximum
                HashMap<Integer, Integer> dist = new HashMap<>();
                graph.getNodes().forEach(x -> dist.put(x.id, Integer.MAX_VALUE));

                // Take a imaginary parent
                HashMap<Integer, Integer> par = new HashMap<>();
                graph.getNodes().forEach(x -> par.put(x.id, -1));

                // Distance of source to source is 0
                dist.replace(i, 0);
                Queue<Integer> q = new LinkedList<>();

                // Push the source element
                q.add(i);

                // Continue until queue is not empty
                while (!q.isEmpty())
                {

                    // Take the first element
                    int x = q.poll();

                    // Traverse for all it's children
                    List<Integer> children = gr.get(x);
                    if(children == null) continue;
                    for (int child : children)
                    {
                        // If it is not visited yet
                        if (dist.get(child) == Integer.MAX_VALUE)
                        {
                            // Increase distance by 1
                            dist.replace(child, dist.get(x) + 1);

                            //New path
                            List<Integer> newPath = new ArrayList<>(shortestCycles.get(x));
                            newPath.add(child);

                            //Replace
                            shortestCycles.replace(child, newPath);

                            // Change parent
                            par.replace(child, x);

                            // Push into the queue
                            q.add(child);
                        } else if (par.get(x) != child && par.get(child) != x)
                            if(ans > dist.get(x) + dist.get(child) + 1){
                                ans = dist.get(x) + dist.get(child) + 1;

                                //Get path
                                List<Integer> newPath = new ArrayList<>(shortestCycles.get(x));
                                newPath.addAll(new ArrayList<>(shortestCycles.get(child)));
                                newPath.add(i);

                                //Set path
                                shortestCycles.replace(i, newPath);
                            }

                    }
                }
            }

            //Return result
            return shortestCycles;
        }


    }


