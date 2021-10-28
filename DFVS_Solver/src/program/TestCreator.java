package program;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class TestCreator {

    public static Graph createFromFile(String path, String filename){
        Graph graph = new Graph(filename);

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

        return graph;
    }

    public static List<Graph> createComplexGraphs() {
        return createFromFolder("src/inputs/complex");
    }

    public static List<Graph> createFromFolder(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        List<Graph> graphs = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            graphs.add(createFromFile(path + "/" , listOfFile.getName()));
        }
        return graphs;
    }

    public static Graph createSimpleDAG(){
        Graph graph = new Graph("S1");

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("B", "C");
        graph.addArc("B", "E");
        graph.addArc("C", "D");

        return graph;
    }

    public static Graph createSimpleNonDAG(){
        Graph graph = new Graph("S2");

        graph.addArc("A", "B");
        graph.addArc("A", "C");
        graph.addArc("B", "C");
        graph.addArc("B", "E");
        graph.addArc("C", "A");
        graph.addArc("E", "A");
        graph.addArc("E", "F");
        graph.addArc("F", "E");

        return graph;
    }


    /**
     * program.Graph with circle A-B-C
     */
    public static Graph createSimpleNonDAG2(){
        Graph graph = new Graph("S3");

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("B", "E");
        graph.addArc("C", "F");

        return graph;
    }


    /**
     * program.Graph with circles A-B-C and E-F-G
     */
    public static Graph createSimpleNonDAG3(){
        Graph graph = new Graph("S4");

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("D", "E");
        graph.addArc("E", "F");
        graph.addArc("F", "G");
        graph.addArc("G", "E");

        return graph;
    }


    /**
     * program.Graph with circles A-B-C and E-F-G, but the circles are connected
     */
    public static Graph createSimpleNonDAG4(){
        Graph graph = new Graph("S4");

        graph.addArc("A", "B");
        graph.addArc("B", "C");
        graph.addArc("C", "A");
        graph.addArc("A", "D");
        graph.addArc("D", "E");
        graph.addArc("E", "A");

        return graph;
    }

    public static Graph createK3Test(){
        Graph graph = new Graph("K3");

        graph.addArc("A", "B");
        graph.addArc("B", "A");
        graph.addArc("C", "D");
        graph.addArc("D", "C");
        graph.addArc("E", "F");
        graph.addArc("F", "E");

        return graph;
    }

}
