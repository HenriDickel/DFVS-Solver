package program.utils;

import program.model.Graph;
import program.log.Log;
import program.model.GraphFile;
import program.model.Instance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public abstract class InstanceCreator {

    private static Instance createInstance(String name, Graph graph, int optimalK) {
        return new Instance(name, graph, optimalK);
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
        return createInstance(file.name, graph, file.optimalK);
    }

    public static List<GraphFile> getFiles(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles == null) throw new RuntimeException("File path '" + path + "' does not exist!");

        List<GraphFile> files = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            files.add(new GraphFile(path, listOfFile.getName(), -1));
        }
        return files;
    }

    public static Instance createPaceInstanceFromSystemIn() {

        Graph graph = new Graph();
        int id = 0;

        Scanner scan = new Scanner(System.in);
        while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] split = line.trim().split(" ");
            if(id == 0) { // first line
                Integer n = Integer.parseInt(split[0]);
                Integer m = Integer.parseInt(split[1]);
            } else {
                for(String outString: split) {
                    if(outString.length() == 0) continue;
                    Integer outId = Integer.parseInt(outString);
                    graph.addArc(id, outId);
                }
            }
            id++;
        }

        return createInstance("PACE", graph, -1);
    }

    public static Instance createFromPaceFile(GraphFile file) {
        Graph graph = new Graph();
        int id = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(file.path + file.name))) {
            for(String line; (line = br.readLine()) != null; ) {
                String[] split = line.trim().split(" ");
                if(id == 0) { // first line
                    Integer n = Integer.parseInt(split[0]);
                    Integer m = Integer.parseInt(split[1]);
                } else {
                    for(String outString: split) {
                        if(outString.length() == 0) continue;
                        Integer outId = Integer.parseInt(outString);
                        graph.addArc(id, outId);
                    }
                }
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createInstance(file.name, graph, file.optimalK);
    }

    public static List<GraphFile> getPaceFilesExact(String startFilename) {
        List<GraphFile> files = getFiles("src/inputs/pace_exact_public/");
        if(startFilename == null) return files;
        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).name.equals(startFilename)) {
                return files.subList(i, files.size());
            }
        }
        throw new RuntimeException("Didn't found instance with name '" + startFilename + "'");
    }

    public static List<GraphFile> getPaceFilesHeuristic(String startFilename) {
        List<GraphFile> files = getFiles("src/inputs/pace_heuristic_public/");
        if(startFilename == null) return files;
        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).name.equals(startFilename)) {
                return files.subList(i, files.size());
            }
        }
        throw new RuntimeException("Didn't found instance with name '" + startFilename + "'");
    }
}
