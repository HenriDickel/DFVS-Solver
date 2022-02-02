package program.log;

import program.model.Instance;
import program.utils.Color;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Log {

    private static final String DEBUG_LOG_PATH = "src/logs/DebugLog.txt";
    private static final String MAIN_LOG_PATH = "src/logs/MainLog.csv";
    private static final String ILP_LOG_PATH = "src/logs/ILPLog.csv";
    private static final String DETAIL_LOG_PATH = "src/logs/DetailLog.csv";
    private static final String HEURISTIC_LOG_PATH = "src/logs/HeuristicLog.csv";

    public static boolean ignore;
    public static int level = 0;

    public static void debugLog(String name, String message){
        debugLog(name, message, Color.WHITE);
    }

    public static void debugLog(String name, String message, Color color){

        //Ignore Log
        if(ignore) return;

        //Create program.log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String gap = " ".repeat(Math.max(0, 38 - name.length()));
        String recursionGap = " - ".repeat(level);
        String logMessage = color + "[" + LocalDateTime.now().format(dtf) + "] " + gap + name + ": " +  recursionGap + message + "\u001B[0m";

        //Console Log
        System.out.println(logMessage);

        //Log File
        try(PrintWriter output = new PrintWriter(new FileWriter(DEBUG_LOG_PATH,true)))
        {
            output.println(logMessage);
        }
        catch (Exception ignored) {}

    }

    public static void debugLogNoBreak(String name, String message) {
        //Ignore Log
        if(ignore) return;

        //Create program.log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String gap = " ".repeat(Math.max(0, 38 - name.length()));
        String recursionGap = " - ".repeat(level);
        String logMessage = "[" + LocalDateTime.now().format(dtf) + "] " + gap + name + ": " +  recursionGap + message;

        //Console Log
        System.out.print(logMessage);

        //Log File
        try(PrintWriter output = new PrintWriter(new FileWriter(DEBUG_LOG_PATH,true)))
        {
            output.println(logMessage);
        }
        catch (Exception ignored) {}
    }

    public static void debugLogAdd(String message, boolean newLine) {

        //Ignore Log
        if(ignore) return;

        //Console Log
        if(newLine) System.out.println(message);
        else System.out.print(message);
    }

    public static void Clear() {
        try {
            new File(DEBUG_LOG_PATH).createNewFile();
            new File(MAIN_LOG_PATH).createNewFile();
            new File(ILP_LOG_PATH).createNewFile();
            new File(DETAIL_LOG_PATH).createNewFile();
            new File(HEURISTIC_LOG_PATH).createNewFile();
            new PrintWriter(DEBUG_LOG_PATH).close();
            new PrintWriter(MAIN_LOG_PATH).close();
            new PrintWriter(ILP_LOG_PATH).close();
            new PrintWriter(HEURISTIC_LOG_PATH).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println("name,n,m,k_optimal,k_solved,k_start,verified,pre_removed_nodes,removed_flowers,recursive_steps,millis,packing_millis");
        }
        catch (Exception ignored) {}
        try(PrintWriter output = new PrintWriter(new FileWriter(ILP_LOG_PATH,true)))
        {
            output.println("name,n,m,pre_removed_nodes,k_start,k_optimal,k_solved,num_constraints,verified,millis");
        }
        catch (Exception ignored) {}
        try(PrintWriter output = new PrintWriter(new FileWriter(DETAIL_LOG_PATH,true)))
        {
            output.println("name,level,cycle_size,recursive_steps");
        }
        catch (Exception ignored) {}
        try(PrintWriter output = new PrintWriter(new FileWriter(HEURISTIC_LOG_PATH,true)))
        {
            output.println("name,k_optimal,k_approx,millis");
        }
        catch (Exception ignored) {}
    }

    public static void heuristicLog(Instance instance, int approxK, long millis) {

        //Ignore Log
        if(ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(HEURISTIC_LOG_PATH,true)))
        {
            output.println(instance.NAME + "," + instance.OPTIMAL_K + "," + approxK + "," + millis);
        }
        catch (Exception ignored) {}
    }

    public static void mainLog(Instance instance, long millis, long packingMillis, boolean verified){

        //Ignore Log
        if(ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println(instance.NAME + "," + instance.N + "," + instance.M + "," + instance.OPTIMAL_K + "," + instance.solvedK + "," + instance.startK + "," + verified + "," + instance.preRemovedNodes + "," + instance.removedFlowers + "," + instance.recursiveSteps + "," + millis + "," + packingMillis);
        }
        catch (Exception ignored) {}
    }

    public static void ilpLog(Instance instance, long millis, boolean verified) {

        //Ignore Log
        if(ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(ILP_LOG_PATH,true)))
        {
            output.println(instance.NAME + "," + instance.N + "," + instance.M + "," + instance.preRemovedNodes + "," + instance.startK + "," + instance.OPTIMAL_K + "," + instance.solvedK + "," + instance.numConstraints + "," + verified + "," + millis);
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
