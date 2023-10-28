package thread;

import conf.ConfigurationManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SoundBoardServerThread implements Runnable {

    private final ServerSocket serverSocket;
    private static SoundBoardServerThread soundBoardServerThread;
    private final List<ClientSocketThread> clientSockets = new ArrayList<>();

    public static SoundBoardServerThread getInstance() {
        if(soundBoardServerThread == null) {
            soundBoardServerThread = new SoundBoardServerThread();
        }

        return soundBoardServerThread;
    }
    private SoundBoardServerThread() {
        try {
            int PORT = Integer.parseInt(ConfigurationManager.getInstance().getProperty("sound.board.socket.port"));
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted : " + clientSocket.getRemoteSocketAddress().toString());
                ClientSocketThread clientSocketThread = new ClientSocketThread(clientSocket);
                Thread thread = new Thread(clientSocketThread);
                thread.start();
                clientSockets.add(clientSocketThread);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void notifySoundRequest(ClientSocketThread clientSocketThreadToExclude, byte[] mp3Data) {
        for(ClientSocketThread clientSocketThread : clientSockets) {
            if(clientSocketThread != clientSocketThreadToExclude) {
                clientSocketThread.notifyClientToPlayAudio(mp3Data);
            }
        }
    }
}
