import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GraphFileManager {

    public static Graph CreateFromFile(String path, String filename){
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


    public static List<Graph> CreateFromFolder(String path) {
        path = path.substring(0, path.length() - 1);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        List<Graph> graphs = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            graphs.add(CreateFromFile(path + "/" , listOfFile.getName()));
        }
        return graphs;
    }

    public static void SaveSolution(String path, String graphName, List<Node> nodes){
        //Add solution to name
        String fullPath = path + "solution_" + graphName;

        //Delete file
        try {
            new PrintWriter(fullPath).close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Save Solution
        try(PrintWriter output = new PrintWriter(new FileWriter(fullPath,true)))
        {
            String solution = nodes.stream().map(x -> x.label).collect(Collectors.joining("\n"));
            output.printf(solution);

        }
        catch (Exception ignored) {}
    }
}
