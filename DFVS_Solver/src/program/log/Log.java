package program.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Log {

    private static final String DEBUG_LOG_PATH = "src/program/log/DebugLog.txt";
    private static final String MAIN_LOG_PATH = "src/program/log/MainLog.csv";

    public static boolean Ignore;

    public static void debugLog(String graphName, String message){
        debugLog(graphName, message, false);
    }

    public static void debugLog(String graphName, String message, boolean error){

        //Ignore Log
        if(Ignore) return;

        //Create program.log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String logMessage = "[" + LocalDateTime.now().format(dtf) + "]" +  " " + graphName + ": \t" + message;

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
            new PrintWriter(DEBUG_LOG_PATH).close();
            new PrintWriter(MAIN_LOG_PATH).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println("name,optimalK,k,millis,verified");
        }
        catch (Exception ignored) {}
    }

    public static void mainLog(String name, int optimalK, int k, long millis, boolean verified){

        //Ignore Log
        if(Ignore) return;

        try(PrintWriter output = new PrintWriter(new FileWriter(MAIN_LOG_PATH,true)))
        {
            output.println(name + "," + optimalK + "," + k + "," + millis + "," + verified);
        }
        catch (Exception ignored) {}
    }

}
