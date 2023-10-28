package thread;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientSocketThread implements Runnable {

    private Socket socket;

    public ClientSocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true) {
            try {
                InputStream inputStream = socket.getInputStream();

                byte header = (byte)inputStream.read();
                int length = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();

                //48 - 0 run audio request
                //49 - 1 new audio add
                //50 - 2 start up audio sync
                if(header == 48) {
                    System.out.println("0");
                    //String name = new String(ByteBuffer.wrap(inputStream.readNBytes(12)).array(), StandardCharsets.UTF_8).trim();
                    byte[] bytes = readData(socket.getInputStream(), length);
                    runAudioRequest(bytes);
                } else if (header == 49) {
                    System.out.println("1");
                    String name = new String(ByteBuffer.wrap(inputStream.readNBytes(12)).array(), StandardCharsets.UTF_8).trim();
                    byte[] bytes = readData(socket.getInputStream(), length);
                    oneType(bytes, name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                //SoundBoardServerThread.getInstance().notifySoundRequest("deleteClientSocketThread", this); //change to a different method
                break;
            }

        }
    }

    private byte[] readData(InputStream inputStream, int length) {
        try{
            byte[] buffer;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int readBytes = 0;
            while(length - readBytes > 0) {
                byte[] newByteArr = new byte[1024];
                readBytes += inputStream.read(newByteArr);
                outputStream.write(newByteArr);
                System.out.println("read bytes = " + readBytes);
            }

            buffer = outputStream.toByteArray();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void oneType(byte[] bytes, String name) throws IOException {
        FileUtils.writeByteArrayToFile(new File("E:\\tests\\" + name + ".mp3"), bytes);

    }

    private void runAudioRequest(byte[] bytes) throws IOException {
        String name = new String(bytes, StandardCharsets.UTF_8).trim();
        System.out.println(name);

        String path = "E:\\tests\\" + name + ".mp3";
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] sendData = new byte[(int)file.length()];
        fileInputStream.read(sendData);

        SoundBoardServerThread.getInstance().notifySoundRequest(this, sendData);
    }


    public void notifyClientToPlayAudio(byte[] mp3Data) {
        try {
            byte[] lengthValueData = new byte[mp3Data.length + 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(lengthValueData);
            byteBuffer.put(ByteBuffer.allocate(4).putInt(mp3Data.length).array());
            byteBuffer.put(mp3Data);
            socket.getOutputStream().write(byteBuffer.array());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
