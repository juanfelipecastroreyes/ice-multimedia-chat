package chat.client;

import ChatApp.NicknameInUse;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.InitializationData;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.Properties;
import com.zeroc.Ice.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ClientMain {

    private static final String CONFIG_FILE = "config.client";
    private static final String DEFAULT_PROXY = "ChatServer:tcp -h localhost -p 10000";

    private ClientMain() {
    }

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, initData())) {
            ChatClient client = new ChatClient(communicator);

            String proxy = communicator.getProperties()
                    .getPropertyWithDefault("ChatServer.Proxy", DEFAULT_PROXY);
            try {
                client.connect(proxy);
            } catch (LocalException e) {
                Console.error("No se pudo conectar al servidor (" + proxy + "): " + e.ice_id());
                return;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(client::logout));

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            if (!askLogin(client, in)) {
                return;
            }

            Console.info("Conectado como " + client.getNickname() + ". Escribe /quit para salir.");
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("/quit")) {
                    break;
                }
            }
            client.logout();
        } catch (IOException e) {
            Console.error("Error leyendo la entrada: " + e.getMessage());
        }
    }

    /** Pide un nickname hasta que el servidor lo acepte. Devuelve false si se cierra la entrada. */
    private static boolean askLogin(ChatClient client, BufferedReader in) throws IOException {
        while (true) {
            Console.println("Nickname:");
            String nick = in.readLine();
            if (nick == null) {
                return false;
            }
            nick = nick.trim();
            if (nick.isEmpty() || nick.contains(" ")) {
                Console.error("El nickname no puede estar vacio ni tener espacios.");
                continue;
            }
            try {
                client.login(nick);
                return true;
            } catch (NicknameInUse e) {
                Console.error("El nickname '" + nick + "' ya esta en uso. Prueba otro.");
            }
        }
    }

    private static InitializationData initData() {
        Properties props = Util.createProperties();
        // Heartbeats constantes y sin cierre por inactividad: la conexion lleva los
        // callbacks, si se cerrara el cliente dejaria de recibir eventos.
        props.setProperty("Ice.ACM.Client.Heartbeat", "3");
        props.setProperty("Ice.ACM.Client.Close", "0");
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            props.load(CONFIG_FILE);
        }
        InitializationData init = new InitializationData();
        init.properties = props;
        return init;
    }
}
