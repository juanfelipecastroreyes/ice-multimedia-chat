package chat.client;

import ChatApp.CallInfo;
import ChatApp.ClientCallback;
import ChatApp.FileInfo;
import ChatApp.Message;
import com.zeroc.Ice.Current;

/**
 * Servant de callbacks: por aqui llega al cliente todo lo que el servidor empuja.
 * Ice despacha estos metodos en sus propios hilos, por eso solo se imprime con Console.
 */
public final class ClientCallbackI implements ClientCallback {

    @Override
    public void onMessage(Message msg, Current current) {
        String time = Console.time(msg.timestamp);
        if (msg.room.isEmpty()) {
            Console.println("[privado " + time + "] " + msg.sender + ": " + msg.text);
        } else {
            Console.println("[#" + msg.room + " " + time + "] " + msg.sender + ": " + msg.text);
        }
    }

    @Override
    public void onUserOnline(String nickname, Current current) {
        Console.info(nickname + " se conecto.");
    }

    @Override
    public void onUserOffline(String nickname, Current current) {
        Console.info(nickname + " se desconecto.");
    }

    @Override
    public void onRoomJoined(String room, String nickname, Current current) {
        Console.info("[#" + room + "] " + nickname + " entro a la sala.");
    }

    @Override
    public void onRoomLeft(String room, String nickname, Current current) {
        Console.info("[#" + room + "] " + nickname + " salio de la sala.");
    }

    @Override
    public void onFileStart(FileInfo info, Current current) {
        String where = info.room.isEmpty() ? "" : " en #" + info.room;
        Console.info(info.sender + " esta enviando " + info.fileName
                + " (" + humanSize(info.size) + ")" + where + ".");
    }

    @Override
    public void onFileChunk(String transferId, int index, byte[] data, Current current) {
        // La reconstruccion del archivo se implementa en el paso de archivos (RF-04).
    }

    @Override
    public void onFileEnd(String transferId, Current current) {
        Console.info("Transferencia " + transferId + " terminada.");
    }

    @Override
    public void onIncomingCall(CallInfo call, Current current) {
        if (call.group) {
            Console.info(call.caller + " inicio una llamada en #" + call.room
                    + ". Usa /accept " + call.callId + " para unirte.");
        } else {
            Console.info("Llamada entrante de " + call.caller
                    + ". /accept " + call.callId + " o /reject " + call.callId);
        }
    }

    @Override
    public void onCallAccepted(int callId, String nickname, Current current) {
        Console.info(nickname + " se unio a la llamada " + callId + ".");
    }

    @Override
    public void onCallRejected(int callId, String nickname, Current current) {
        Console.info(nickname + " rechazo la llamada " + callId + ".");
    }

    @Override
    public void onCallLeft(int callId, String nickname, Current current) {
        Console.info(nickname + " salio de la llamada " + callId + ".");
    }

    @Override
    public void onCallEnded(int callId, Current current) {
        Console.info("La llamada " + callId + " termino.");
    }

    static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
