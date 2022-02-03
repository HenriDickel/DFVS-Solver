package program.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class Timer {

    private static long startMillis;
    private static long endMillis;

    public static void start(int timeLimit) {
        startMillis = System.currentTimeMillis();
        endMillis = startMillis + (timeLimit * 1000L);
    }

    public static long getSecondsLeft() {
        return (endMillis - System.currentTimeMillis()) / 1000;
    }

    public static boolean isTimeout() {
        return System.currentTimeMillis() > endMillis;
    }

    public static long getMillis() {
        return System.currentTimeMillis() - startMillis;
    }

    public static String format(long millis) {
        return String.format("%.3f", millis / 1000.0f);
    }
}
