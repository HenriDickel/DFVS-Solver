package program.utils;

import program.model.Graph;
import program.log.Log;
import program.model.GraphFile;
import program.model.Instance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public abstract class InstanceCreator {

    private static final String DATASET = "dataset_3";
    private static final String COMPLEX_PATH = "src/inputs/" + DATASET + "/complex/";
    private static final String SYNTHETIC_PATH = "src/inputs/" + DATASET + "/synthetic/";

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
                Scanner scan = new Scanner(new File("src/inputs/" + DATASET + "/optimal_solution_sizes.txt"));
                while(scan.hasNextLine()){
                    String line = scan.nextLine();
                    String name = line.split(" {5}")[0];
                    String optimalK = line.split(" {5}")[1];
                    if(optimalK.equals("time limit reached")) optimalKMap.put(name, -1);
                    else optimalKMap.put(name, Integer.parseInt(optimalK));
                }
            } catch (IOException e) {
                Log.debugLog(graphName, "Couldn't read  file 'optimal_solution_sizes.txt'", true);
                return -1;
            }
        }
        Integer optimalK = optimalKMap.get(graphName);
        return (optimalK != null) ? optimalK : -1;
    }

    public static Instance createFromFile(GraphFile file){
        Graph graph = new Graph();

        try (Stream<String> stream = Files.lines(Paths.get(file.path + file.name))) {
            stream.forEach(str -> {
                if(str.startsWith("#")) return;
                if(str.startsWith("%")) return;

                //Add Line
                String[] split = str.trim().split(" ");
                try {
                    Integer nodeId1 = Integer.parseInt(split[0]);
                    Integer nodeId2 = Integer.parseInt(split[1]);
                    graph.addArc(nodeId1, nodeId2);
                }
                catch (NumberFormatException e)
                {
                    throw new RuntimeException("Couldn't parse file '" + file.name + "' to Integer");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createInstance(file.name, graph);
    }

    public static List<GraphFile> getErrorFilesDataset2() {
        List<GraphFile> files = new ArrayList<>();

        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_100-m_346-k_30-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_160-m_810-k_25-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_180-m_1022-k_30-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_180-m_1057-k_25-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_200-m_1319-k_30-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_60-m_271-k_20-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_70-m_312-k_20-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_70-m_347-k_25-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_80-m_402-k_20-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_90-m_327-k_30-p_0.05.txt"));


        return files;
    }

    public static List<GraphFile> getSelectedFilesDataset3() {
        List<GraphFile> files = new ArrayList<>();
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1600-m_273042-k_70-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_150-m_1423-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_27142-k_50-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_46137-k_70-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_124702-k_200-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_29037-k_100-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_30042-k_120-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_47847-k_100-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_116757-k_120-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1000-m_30795-k_150-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_178841-k_50-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_99879-k_150-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_150-m_1561-k_70-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_200-m_1336-k_50-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_104213-k_200-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_1300-m_95352-k_100-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_2000-m_422023-k_70-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_2600-m_716555-k_100-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_3000-m_240581-k_120-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_4000-m_1721128-k_200-p_0.2.txt"));
        files.add(new GraphFile(COMPLEX_PATH, "usairport-n_1577"));
        files.add(new GraphFile(COMPLEX_PATH, "blogs-n_1227"));
        files.add(new GraphFile(COMPLEX_PATH, "chess-n_1500"));
        files.add(new GraphFile(COMPLEX_PATH, "chess-n_2000"));
        files.add(new GraphFile(COMPLEX_PATH, "health-n_2000"));
        files.add(new GraphFile(COMPLEX_PATH, "link-kv-n_1000"));
        files.add(new GraphFile(COMPLEX_PATH, "link-kv-n_5000"));
        files.add(new GraphFile(COMPLEX_PATH, "wikispeedia-n_4181"));
        return files;
    }

    public static List<GraphFile> getSelectedFilesDataset2() {
        List<GraphFile> files = new ArrayList<>();
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_100-m_670-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_140-m_632-k_30-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_180-m_4031-k_30-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_200-m_2488-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_200-m_4765-k_30-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_250-m_3612-k_25-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_250-m_3681-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_275-m_2220-k_30-p_0.05.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_275-m_4371-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_275-m_8453-k_25-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_300-m_5197-k_30-p_0.1.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_300-m_9999-k_25-p_0.2.txt"));
        files.add(new GraphFile(SYNTHETIC_PATH, "synth-n_80-m_483-k_30-p_0.1.txt"));
        files.add(new GraphFile(COMPLEX_PATH, "biology-n_56-m_1372-p_0.5-4"));
        files.add(new GraphFile(COMPLEX_PATH, "biology-n_56-m_1372-p_0.75-4"));
        files.add(new GraphFile(COMPLEX_PATH, "biology-n_77-m_1411-p_0.5-5"));
        files.add(new GraphFile(COMPLEX_PATH, "biology-n_81-m_1292-p_0.75-3"));
        files.add(new GraphFile(COMPLEX_PATH, "blogs-n_1000"));
        files.add(new GraphFile(COMPLEX_PATH, "email"));
        files.add(new GraphFile(COMPLEX_PATH, "usairport-n_1000"));
        return files;
    }

    public static List<GraphFile> getUnsolvedFilesDataset2(){
        List<GraphFile> files = new ArrayList<>();
        files.add(new GraphFile(COMPLEX_PATH, "oz"));
        files.add(new GraphFile(COMPLEX_PATH, "celegansneural"));
        return files;
    }

    public static List<GraphFile> getComplexAndSyntheticFiles(String startFilename) {
        List<GraphFile> files = getFiles(SYNTHETIC_PATH);
        files.addAll(getFiles(SYNTHETIC_PATH));
        if(startFilename == null) return files;
        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).name.equals(startFilename)) {
                return files.subList(i, files.size());
            }
        }
        throw new RuntimeException("Didn't found instance with name '" + startFilename + "'");
    }

    public static List<GraphFile> getFiles(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null) throw new RuntimeException("File path '" + path + "' does not exist!");

        List<GraphFile> files = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            files.add(new GraphFile(path, listOfFile.getName()));
        }
        return files;
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
     * Creates a BFS test graph.
     */
    public static Instance createBFSTest1(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 4);
        graph.addArc(4, 1);

        graph.addArc(2, 5);
        graph.addArc(5, 6);
        graph.addArc(6, 3);

        return createTestInstance("bfs_test_1", graph, 1);
    }

    /**
     * Creates a BFS test graph.
     */
    public static Instance createBFSTest2(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 4);
        graph.addArc(4, 1);

        graph.addArc(2, 5);
        graph.addArc(5, 6);
        graph.addArc(6, 3);

        graph.addArc(5, 7);
        graph.addArc(7, 5);

        return createTestInstance("bfs_test_1", graph, 1);
    }

    /**
     * Creates a simple acyclic graph.
     */
    public static Instance createSimpleDAG(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 3);
        graph.addArc(2, 3);
        graph.addArc(2, 5);
        graph.addArc(3, 4);

        return createTestInstance("simple_n4_m5_k0", graph, 0);
    }

    /**
     * Creates a simple acyclic graph.
     */
    public static Instance createSimpleNonDAG1(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 3);
        graph.addArc(2, 3);
        graph.addArc(2, 5);
        graph.addArc(3, 1);
        graph.addArc(5, 1);
        graph.addArc(5, 6);
        graph.addArc(6, 5);

        return createTestInstance("simple-n6_m8_k2", graph, 2);
    }

    /**
     * Creates graph with circle A-B-C.
     */
    public static Instance createSimpleNonDAG2(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 1);
        graph.addArc(1, 4);
        graph.addArc(2, 5);
        graph.addArc(3, 6);

        return createTestInstance("simple_n6_m6_k1", graph, 1);
    }


    /**
     * Creates graph with circles A-B-C and E-F-G.
     */
    public static Instance createSimpleNonDAG3(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 1);
        graph.addArc(1, 4);
        graph.addArc(4, 5);
        graph.addArc(5, 6);
        graph.addArc(6, 7);
        graph.addArc(7, 5);

        return createTestInstance("simple-n7_m8_k2", graph, 2);
    }


    /**
     * Creates graph with circles A-B-C and E-F-G, but the circles are connected.
     */
    public static Instance createSimpleNonDAG4(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 1);
        graph.addArc(1, 4);
        graph.addArc(4, 5);
        graph.addArc(5, 1);

        return createTestInstance("simple-n5_m6_k1", graph, 1);
    }

    /**
     * Creates a graph of 3 fully connected tuples.
     */
    public static Instance createSimpleNonDAG5(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 1);
        graph.addArc(3, 4);
        graph.addArc(4, 3);
        graph.addArc(5, 6);
        graph.addArc(6, 5);

        return createTestInstance("simple-n6_m6_k3", graph, 3);
    }

    /**
     *
     */
    public static Instance createSimpleNonDAG6(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 1);
        graph.addArc(2, 3);
        graph.addArc(3, 2);

        graph.addArc(4, 5);
        graph.addArc(5, 4);
        graph.addArc(5, 6);
        graph.addArc(6, 5);

        graph.addArc(7, 8);
        graph.addArc(8, 7);
        graph.addArc(8, 9);
        graph.addArc(9, 8);

        graph.addArc(0, 1);
        graph.addArc(1, 0);
        graph.addArc(0, 4);
        graph.addArc(4, 0);
        graph.addArc(0, 7);
        graph.addArc(7, 0);

        return createTestInstance("simple-n7_m12_k3", graph, 3);
    }

    /**
     *
     */
    public static Instance createSimpleNonDAG7(){
        Graph graph = new Graph();

        graph.addArc(0, 1);
        graph.addArc(3, 0);
        graph.addArc(0, 4);
        graph.addArc(6, 0);

        graph.addArc(1, 3);
        graph.addArc(3, 1);

        graph.addArc(4, 6);
        graph.addArc(6, 4);

        graph.addArc(3, 6);
        graph.addArc(6, 3);

        graph.addArc(1, 4);
        graph.addArc(4, 1);

        return createTestInstance("simple-n6_m6_k3", graph, 3);
    }

    /**
     * Creates a fully connected graph with 3 nodes.
     */
    public static Instance createFullConnected3(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 3);
        graph.addArc(2, 1);
        graph.addArc(2, 3);
        graph.addArc(3, 1);
        graph.addArc(3, 2);

        return createTestInstance("full-n3_m6_k2", graph, 2);
    }

    /**
     * Creates a fully connected graph with 3 nodes.
     */
    public static Instance createFullConnected4(){
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 3);
        graph.addArc(1, 4);
        graph.addArc(2, 1);
        graph.addArc(2, 3);
        graph.addArc(2, 4);
        graph.addArc(3, 1);
        graph.addArc(3, 2);
        graph.addArc(3, 4);
        graph.addArc(4, 1);
        graph.addArc(4, 2);
        graph.addArc(4, 3);

        return createTestInstance("full-n4_m12_k3", graph, 3);
    }

    /**
     * Creates a simple graph for testing the BFS.
     */
    public static Instance createBFSDAG1() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 3);
        graph.addArc(1, 4);
        graph.addArc(2, 6);
        graph.addArc(3, 5);
        graph.addArc(4, 5);
        graph.addArc(5, 7);
        graph.addArc(6, 1);
        graph.addArc(7, 1);

        return createTestInstance("bfs-n7_m9_k1", graph, 1);
    }

    /**
     * Creates a simple graph for testing the BFS.
     */
    public static Instance createBFSDAG2() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 4);
        graph.addArc(4, 2);

        return createTestInstance("bfs-n4_m4_k1", graph, 1);
    }

    /**
     * Creates graph with circles A-B-C-D and B-C.
     * The BFS should find the circle B-C.
     */
    public static Instance createBFSDAG3() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 4);
        graph.addArc(4, 1);
        graph.addArc(3, 2);

        return createTestInstance("bfs-n4_m5_k1", graph, 1);
    }

    public static Instance createFlower1() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(1, 0);
        graph.addArc(3, 1);
        graph.addArc(2, 1);
        graph.addArc(0, 3);

        return createTestInstance("flower1", graph, 1);
    }

    public static Instance createFlower2() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 1);

        graph.addArc(1, 3);
        graph.addArc(3, 1);

        graph.addArc(1, 4);
        graph.addArc(4, 1);

        return createTestInstance("flower2", graph, 1);
    }

    public static Instance createFlower3() {
        Graph graph = new Graph();

        graph.addArc(1, 2);
        graph.addArc(2, 1);

        graph.addArc(1, 3);
        graph.addArc(3, 1);

        graph.addArc(1, 4);
        graph.addArc(4, 1);

        graph.addArc(4, 5);
        graph.addArc(5, 6);
        graph.addArc(6, 4);

        return createTestInstance("flower3", graph, 1);
    }
}
