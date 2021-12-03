package program.log;

import program.model.Instance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Log {

    private static final String DEBUG_LOG_PATH = "src/logs/DebugLog.txt";
    private static final String MAIN_LOG_PATH = "src/logs/MainLog.csv";
    private static final String DETAIL_LOG_PATH = "src/logs/DetailLog.csv";

    public static boolean ignore;
    public static int level = 0;

    public static void debugLog(String graphName, String message){
        debugLog(graphName, message, false);
    }

    public static void debugLog(String graphName, String message, boolean error){

        //Ignore Log
        if(ignore) return;

        //Create program.log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String gap = " ".repeat(Math.max(0, 34 - graphName.length()));
        String recursionGap = " - ".repeat(level);
        String logMessage = "[" + LocalDateTime.now().format(dtf) + "] " + gap + graphName + ": " +  recursionGap + message;

        //Console Log
        if(error) System.err.println(logMessage);
        else System.out.println(logMessage);

        //Log File
        try(PrintWriter output = new PrintWriter(new FileWriter(DEBUG_LOG_PATH,true)))
        {
            output.println(logMessage);
        }
        catch (Exception ignored) {}

    }

    public static void Clear() {
        try {
            new File(DEBUG_LOG_PATH).createNewFile();
            new File(MAIN_LOG_PATH).createNewFile();
            new File(DETAIL_LOG_PATH).createNewFile();
            new PrintWriter(DEBUG_LOG_PATH).close();
            new PrintWriter(MAIN_LOG_PATH).close();
            new PrintWriter(DETAIL_LOG_PATH).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println("name,n,m,k_optimal,k_solved,verified,k_start,pre_deleted_nodes,flower_deleted_nodes,recursive_steps,millis");
        }
        catch (Exception ignored) {}
        try(PrintWriter output = new PrintWriter(new FileWriter(DETAIL_LOG_PATH,true)))
        {
            output.println("name,level,cycle_size,recursive_steps");
        }
        catch (Exception ignored) {}
    }

    public static void mainLog(Instance instance, long millis, boolean verified){

        //Ignore Log
        if(ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println(instance.NAME + "," + instance.N + "," + instance.M + "," + instance.OPTIMAL_K + "," + instance.solvedK + "," + verified + "," + instance.startK + "," + instance.preDeletedNodes + "," + instance.flowerDeletedNodes + "," + instance.recursiveSteps + "," + millis);
        }
        catch (Exception ignored) {}
    }

    public static void detailLog(Instance instance) {

        //Ignore Log
        if(ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(DETAIL_LOG_PATH,true)))
        {
            for(int i = 0; i < instance.averageCycleSize.length; i++) {
                output.println(instance.NAME + "," + i + "," + instance.averageCycleSize[i] + "," + instance.recursiveStepsPerK[i]);
            }
        }
        catch (Exception ignored) {}
    }
}
