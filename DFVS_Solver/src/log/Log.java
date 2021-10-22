package log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Log {

    private static final String LOG_PATH = "src/log/log.txt";
    private static final String TIME_LOG_PATH = "src/log/timeLog.csv";

    public enum LogType {Ignore, Console, File}
    public enum LogDetail {Important, Normal, Unimportant}

    public static LogType type = LogType.Console;
    public static LogDetail detail = LogDetail.Important;

    private static int recursionLevel;

    public static void log(LogDetail detailLevel, String graphName, int recursionLevel, String message) {
        Log.recursionLevel = recursionLevel;
        log(detailLevel, graphName, message);
    }

    public static void log(LogDetail detailLevel, String graphName, String message){

        //Don't Log
        if(type == LogType.Ignore) return;
        if(detailLevel == LogDetail.Normal && detail == LogDetail.Important) return;
        if(detailLevel == LogDetail.Unimportant && detail == LogDetail.Important) return;
        if(detailLevel == LogDetail.Unimportant && detail == LogDetail.Normal) return;

        //Create log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String recursionGap = new String(new char[recursionLevel]).replace("\0", "  ⋅  ");
        String logMessage = "[" + LocalDateTime.now().format(dtf) + "]" +  " " + graphName + ": \t" + recursionGap + message + "\n";

        //Console Log
        if(type == LogType.Console || type == LogType.File){
            System.out.print(logMessage);
        }

        //Log File
        if(type == LogType.File){
            try(PrintWriter output = new PrintWriter(new FileWriter(LOG_PATH,true)))
            {
                output.printf(logMessage);
            }
            catch (Exception ignored) {}
        }

    }

    public static void TimeLog(int k, long timeInMilli){
        try(PrintWriter output = new PrintWriter(new FileWriter(TIME_LOG_PATH,true)))
        {
            output.println(k + " " + timeInMilli);
        }
        catch (Exception ignored) {}
    }

    public static void Clear() {
        try {
            new PrintWriter(LOG_PATH).close();
            new PrintWriter(TIME_LOG_PATH).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(PrintWriter output = new PrintWriter(new FileWriter(TIME_LOG_PATH,true)))
        {
            output.println("k millis");
        }
        catch (Exception ignored) {}
    }

}
