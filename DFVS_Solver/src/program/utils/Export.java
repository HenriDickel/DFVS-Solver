package program.utils;

import program.model.Graph;
import program.model.Instance;
import program.model.Node;

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Export {

    private static final String EXPORT_PATH = "src/exports/";

    public static void ExportGraph(Instance instance, String exportName){

        //Get all nodes
        List<Node> nodes = instance.subGraphs.stream().map(Graph::getNodes).flatMap(List::stream).collect(Collectors.toList());

        //Sort
        nodes.sort(Comparator.comparingInt(x -> x.id));

        //File
        try{
            //Create File
            String path = EXPORT_PATH + instance.NAME + "_" + exportName;
            File file = new File(path);
            if(file.exists()) file.delete();
            file.createNewFile();

            //Use file writer
            FileWriter writer = new FileWriter(path);

            //Get size
            int n = nodes.size();
            int m = n == 0 ? 0 : nodes.stream().map(Node::getOutIdCount).reduce(0, Integer::sum);
            writer.write(n +  " " + m  + "\n");

            //Check if graph is not empty
            if(nodes.size() == 0){
                writer.close();
                return;
            }

            //Add nodes to file
            for(int i = 1; i < nodes.get(nodes.size() - 1).id; i++){

                //Ignore removed nodes
                int finalI = i;
                if(nodes.stream().anyMatch(x -> x.id == finalI)){
                    Node currNode = nodes.stream().filter(x -> x.id == finalI).findAny().get();
                    String out = currNode.getOutIds().stream().map(Object::toString).collect(Collectors.joining(" "));
                    writer.write(out + "\n");
                }
                else writer.write("\n"); //Does not get called because nodes never get deleted completely
            }

            //Close writer
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //Log
        System.out.println("Exported: " + instance.NAME + "_" + exportName);
    }
}
