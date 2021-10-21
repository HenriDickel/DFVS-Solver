package com.company;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Log {

    public enum LogType {Ignore, Console, File}
    public enum LogDetail {Important, Normal, Unimportant}

    public static LogType type = LogType.Console;
    public static LogDetail detail = LogDetail.Important;

    public static void log(LogDetail detailLevel, String graphName, String message){

        //Don't Log
        if(type == LogType.Ignore) return;
        if(detailLevel == LogDetail.Normal && detail == LogDetail.Important) return;
        if(detailLevel == LogDetail.Unimportant && detail == LogDetail.Important) return;
        if(detailLevel == LogDetail.Unimportant && detail == LogDetail.Normal) return;

        //Create log string
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String logMessage = "[" + LocalDateTime.now().format(dtf) + "]" +  " " + graphName + ": " + message + "\n";

        //Console Log
        if(type == LogType.Console || type == LogType.File){
            System.out.print(logMessage);
        }

        //Log File
        if(type == LogType.File){
            try(PrintWriter output = new PrintWriter(new FileWriter("src/com/company/log.txt",true)))
            {
                output.printf(logMessage);
            }
            catch (Exception ignored) {}
        }

    }

    public static void Clear() {
        try {
            new PrintWriter("src/com/company/log.txt").close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
