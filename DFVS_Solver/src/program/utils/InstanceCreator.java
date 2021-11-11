package program.utils;

import program.model.Graph;
import program.model.Instance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public abstract class InstanceCreator {

    private static Instance createInstance(String name, Graph graph) {
        int optimalK = readOptimalKFromFile(name);
        return new Instance(name, graph, optimalK);
    }

    private static Instance createTestInstance(String name, Graph graph, int optimalK) {
        return new Instance(name, graph, optimalK);
    }

    public static Map<String, Integer> optimalKMap = new HashMap<>();

    public static int readOptimalKFromFile(String graphName){
        if(optimalKMap.isEmpty()) {
            try{
                Scanner scan = new Scanner(new File("src/inputs/optimal_solution_sizes.txt"));
                while(scan.hasNextLine()){
                    String line = scan.nextLine();
                    String name = line.split(" {5}")[0];
                    String optimalK = line.split(" {5}")[1];
                    optimalKMap.put(name, Integer.parseInt(optimalK));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return optimalKMap.get(graphName);
    }

    public static Instance createFromFile(String path, String filename){
        Graph graph = new Graph();

        try (Stream<String> stream = Files.lines(Paths.get(path + filename))) {
            stream.forEach(str -> {
                if(str.startsWith("#")) return;
                if(str.startsWith("%")) return;

                //Add Line
                String[] split = str.trim().split(" ");
                graph.addArc(split[0], split[1]);

            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createInstance(filename, graph);
    }

    public static List<Instance> createComplexInstances() {
        return createFromFolder("src/inputs/complex");
    }

    public static List<Instance> createSyntheticInstances() {
        return createFromFolder("src/inputs/synthetic");
    }

    private static List<Instance> createFromFolder(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null) throw new RuntimeException("File path '" + path + "' does not exist!");

        List<Instance> instances = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            instances.add(createFromFile(path + "/" , listOfFile.getName()));
        }
        return instances;
    }

    /**
     * Creates all test instances.
     */
    public static List<Instance> createTestInstances() {
        List<Instance> instances = new ArrayList<>();

        instances.add(createSimpleDAG());
        instances.add(createSimpleNonDAG1());
        instances.add(createSimpleNonDAG2());
        instances.add(createSimpleNonDAG3());
        instances.add(createSimpleNonDAG4());
        instances.add(createSimpleNonDAG5());
        instances.add(createFullConnected3());
        instances.add(createFullConnected4());
        instances.add(createBFSDAG1());
        instances.add(createBFSDAG2());
        instances.add(createBFSDAG3());

        return instances;
    }

    /**
     * Creates a simple acyclic graph.
     */
    public static Instance createSimpleDAG(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("B", "C");
        graph.addArc("B", "E");
        graph.addArc("C", "D");

        return createTestInstance("simple_n4_m5_k0", graph, 0);
    }

    /**
     * Creates a simple acyclic graph.
     */
    public static Instance createSimpleNonDAG1(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("B", "C");
        graph.addArc("B", "E");
        graph.addArc("C", "A");
        graph.addArc("E", "A");
        graph.addArc("E", "F");
        graph.addArc("F", "E");

        return createTestInstance("simple-n6_m8_k2", graph, 2);
    }

    /**
     * Creates graph with circle A-B-C.
     */
    public static Instance createSimpleNonDAG2(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("B", "E");
        graph.addArc("C", "F");

        return createTestInstance("simple_n6_m6_k1", graph, 1);
    }


    /**
     * Creates graph with circles A-B-C and E-F-G.
     */
    public static Instance createSimpleNonDAG3(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("D", "E");
        graph.addArc("E", "F");
        graph.addArc("F", "G");
        graph.addArc("G", "E");

        return createTestInstance("simple-n7_m8_k2", graph, 2);
    }


    /**
     * Creates graph with circles A-B-C and E-F-G, but the circles are connected.
     */
    public static Instance createSimpleNonDAG4(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("D", "E");
        graph.addArc("E", "A");

        return createTestInstance("simple-n5_m6_k1", graph, 1);
    }

    /**
     * Creates a graph of 3 fully connected tuples.
     */
    public static Instance createSimpleNonDAG5(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "A");
        graph.addArc("C", "D");
        graph.addArc("D", "C");
        graph.addArc("E", "F");
        graph.addArc("F", "E");

        return createTestInstance("simple-n6_m6_k3", graph, 3);
    }

    /**
     * Creates a fully connected graph with 3 nodes.
     */
    public static Instance createFullConnected3(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("B", "A");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("C", "B");

        return createTestInstance("full-n3_m6_k2", graph, 2);
    }

    /**
     * Creates a fully connected graph with 3 nodes.
     */
    public static Instance createFullConnected4(){
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("A", "D");
        graph.addArc("B", "A");
        graph.addArc("B", "C");
        graph.addArc("B", "D");
        graph.addArc("C", "A");
        graph.addArc("C", "B");
        graph.addArc("C", "D");
        graph.addArc("D", "A");
        graph.addArc("D", "B");
        graph.addArc("D", "C");

        return createTestInstance("full-n4_m12_k3", graph, 3);
    }

    /**
     * Creates a simple graph for testing the BFS.
     */
    public static Instance createBFSDAG1() {
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("A", "D");
        graph.addArc("B", "F");
        graph.addArc("C", "E");
        graph.addArc("D", "E");
        graph.addArc("E", "G");
        graph.addArc("F", "A");
        graph.addArc("G", "A");

        return createTestInstance("bfs-n7_m9_k1", graph, 1);
    }

    /**
     * Creates a simple graph for testing the BFS.
     */
    public static Instance createBFSDAG2() {
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "D");
        graph.addArc("D", "B");

        return createTestInstance("bfs-n4_m4_k1", graph, 1);
    }

    /**
     * Creates graph with circles A-B-C-D and B-C.
     * The BFS should find the circle B-C.
     */
    public static Instance createBFSDAG3() {
        Graph graph = new Graph();

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "D");
        graph.addArc("D", "A");
        graph.addArc("C", "B");

        return createTestInstance("bfs-n4_m5_k1", graph, 1);
    }
}
