package com.company;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class GraphFileReader {

    public static Graph CreateFromFile(String path){
        Graph graph = new Graph();

        try (Stream<String> stream = Files.lines(Paths.get(path))) {
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
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        List<Graph> graphs = new ArrayList<>();
        for(File file : listOfFiles){
            graphs.add(CreateFromFile(path + "/" + file.getName()));
        }
        return graphs;
    }
}
