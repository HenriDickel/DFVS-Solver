import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Timer {

    private static LocalDateTime startTime;

    private static final long timeout = 4; // in minutes

    public static void start() {
        startTime = LocalDateTime.now();
    }

    public static boolean isTimeout() {
        return ChronoUnit.MINUTES.between(startTime, LocalDateTime.now()) >= timeout;
    }

    public static String getTimeString() {
        long seconds = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        long millis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
        return (seconds == 0) ? millis + "ms" : seconds + "." + millis + "s";
    }

    public static long getMillis() {
        return ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
    }
}
