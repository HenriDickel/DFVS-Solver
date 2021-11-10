package program.log;

import program.algo.MinMaxK;
import program.model.Instance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Statistics {

    private static final String OVERVIEW_MIN_MAX_K_PATH = "src/logs/OverviewMinMaxK.csv";

    public static void CreateOverviewMinMaxK(List<Instance> instances){
        //Create File
        CreateFileAndClear(OVERVIEW_MIN_MAX_K_PATH, "name,n,m,min,optimal,max");

        //Fill File
        try(PrintWriter output = new PrintWriter(new FileWriter(OVERVIEW_MIN_MAX_K_PATH,true)))
        {
            for(Instance instance : instances){
                int min = MinMaxK.minK(instance.N, instance.M);
                int max = MinMaxK.maxK(instance.N, instance.M);
                output.println(instance.NAME + "," + instance.N + "," + instance.M + "," + min + "," + instance.OPTIMAL_K + "," + max);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void CreateFileAndClear(String path, String header){
        //Create File
        try {
            boolean created = new File(path).createNewFile();
            new PrintWriter(path).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Header
        try(PrintWriter output = new PrintWriter(new FileWriter(path,true)))
        {
            output.println(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
