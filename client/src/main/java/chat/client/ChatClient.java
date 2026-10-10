package chat.client;

import ChatApp.ChatServerPrx;
import ChatApp.ClientCallbackPrx;
import ChatApp.NicknameInUse;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Connection;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.ObjectAdapter;

/**
 * Sesion del cliente con el servidor Ice.
 *
 * Los callbacks viajan por la misma conexion que abre el cliente (bidireccional):
 * el adaptador se crea sin endpoints y se asocia a la conexion con setAdapter,
 * asi el cliente no abre ningun puerto y funciona aunque este detras de un NAT.
 */
public final class ChatClient {

    private final Communicator communicator;
    private ChatServerPrx server;
    private ClientCallbackPrx callbackPrx;
    private volatile String nickname;
    private volatile boolean closing;

    public ChatClient(Communicator communicator) {
        this.communicator = communicator;
    }

    /** Se conecta al servidor y registra el servant de callbacks en la conexion. */
    public void connect(String proxyString) {
        server = ChatServerPrx.checkedCast(communicator.stringToProxy(proxyString));
        if (server == null) {
            throw new IllegalStateException("El proxy no corresponde a un ChatServer: " + proxyString);
        }

        ObjectAdapter adapter = communicator.createObjectAdapter("");
        callbackPrx = ClientCallbackPrx.uncheckedCast(adapter.addWithUUID(new ClientCallbackI()));
        adapter.activate();

        Connection connection = server.ice_getConnection();
        connection.setAdapter(adapter);
        connection.setCloseCallback(con -> onConnectionLost());
    }

    public void login(String nick) throws NicknameInUse {
        server.login(nick, callbackPrx);
        nickname = nick;
    }

    /** Cierre ordenado: avisa al servidor para que libere el proxy y notifique a los demas. */
    public void logout() {
        closing = true;
        if (nickname == null || server == null) {
            return;
        }
        try {
            server.logout();
        } catch (LocalException e) {
            // El servidor ya no responde; no hay nada que limpiar de este lado.
        }
        nickname = null;
    }

    public boolean isLoggedIn() {
        return nickname != null;
    }

    public String getNickname() {
        return nickname;
    }

    public ChatServerPrx server() {
        return server;
    }

    private void onConnectionLost() {
        if (closing) {
            return;
        }
        Console.error("Se perdio la conexion con el servidor.");
        // Se sale desde otro hilo: este corre en un hilo de Ice y destroy() lo esperaria.
        new Thread(() -> System.exit(1)).start();
    }
}
