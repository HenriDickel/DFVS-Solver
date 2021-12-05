package program.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class Timer {

    private static LocalDateTime startTime;

    public static final long timeout = 3; // in minutes

    public static void start() {
        startTime = LocalDateTime.now();
    }

    public static Long stop(){
        Long time = getMillis();
        startTime = LocalDateTime.now();
        return time;
    }

    public static boolean isTimeout() {
        return ChronoUnit.MINUTES.between(startTime, LocalDateTime.now()) >= timeout;
    }

    public static long getMillis() {
        return ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
    }

    public static String format(Long millis) {
        return (millis > 1000) ? millis / 1000 + "s" :  millis + "ms";
    }
}
