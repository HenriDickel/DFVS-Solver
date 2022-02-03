package program.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class Timer {

    private static LocalDateTime startTime;

    public static final long timeout = 90; // in seconds

    public static void start() {
        startTime = LocalDateTime.now();
    }

    public static Long stop(){
        Long time = getMillis();
        startTime = LocalDateTime.now();
        return time;
    }

    public static long getSecondsLeft() {
        return timeout - ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    public static boolean isTimeout() {
        return ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()) >= timeout;
    }

    public static long getMillis() {
        return ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
    }

    public static String format(Long millis) {
        return String.format("%.3f",millis / 1000.0f);
    }
}
