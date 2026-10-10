package chat.client;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Salida de consola segura entre hilos.
 * La usan a la vez el hilo que lee comandos y los hilos de Ice que despachan
 * los callbacks, asi que cada linea se imprime completa sin mezclarse.
 */
public final class Console {

    private static final Object LOCK = new Object();
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private Console() {
    }

    public static void println(String line) {
        synchronized (LOCK) {
            System.out.println(line);
            System.out.flush();
        }
    }

    public static void info(String message) {
        println("* " + message);
    }

    public static void error(String message) {
        println("! " + message);
    }

    public static String time(long epochMillis) {
        return TIME.format(Instant.ofEpochMilli(epochMillis));
    }
}
