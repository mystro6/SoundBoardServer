package thread;

import conf.ConfigurationManager;
import consts.ConfConstants;
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

                byte type = (byte)inputStream.read();

                //48 - 0 run audio request
                //49 - 1 new audio add
                //50 - 2 start up audio sync
                if(type == 48) {
                    int length = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();
                    System.out.println("0");
                    //String name = new String(ByteBuffer.wrap(inputStream.readNBytes(12)).array(), StandardCharsets.UTF_8).trim();
                    byte[] bytes = readData(socket.getInputStream(), length);
                    runAudioRequest(bytes);
                } else if (type == 49) {
                    int length = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();
                    System.out.println("1");
                    String name = new String(ByteBuffer.wrap(inputStream.readNBytes(12)).array(), StandardCharsets.UTF_8).trim();
                    byte[] bytes = readData(socket.getInputStream(), length);
                    saveAudio(bytes, name);
                } else if(type == 50) {
                    System.out.println("2");
                    startUpSyncRequest();
                }
            } catch (IOException e) {
                e.printStackTrace();
                SoundBoardServerThread.getInstance().removeSocketFromList(this); //change to a different method
                break;
            }

        }
    }

    private void startUpSyncRequest() {
        byte[] audioNamesData = getAudioNamesString();
        try {
            byte[] lengthValueData = new byte[audioNamesData.length + 4 + 1];
            byte[] typeByte = new byte[] {50};
            ByteBuffer byteBuffer = ByteBuffer.wrap(lengthValueData);
            byteBuffer.put(typeByte);
            byteBuffer.put(ByteBuffer.allocate(4).putInt(audioNamesData.length).array());
            byteBuffer.put(audioNamesData);
            socket.getOutputStream().write(byteBuffer.array());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getAudioNamesString() {
        final String pipe = "|";
        File directory = new File(ConfigurationManager.getInstance().getProperty(ConfConstants.FILE_PATH));
        String[] audioFileNames = directory.list();
        StringBuilder builder = new StringBuilder();
        for(String audioName : audioFileNames) {
            builder.append(audioName);
            builder.append(pipe);
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] readData(InputStream inputStream, int length) {
        final int maxReadByteAtOnce = 1024;

        try{
            byte[] buffer;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int readBytes = 0;
            while(length - readBytes > 0) {
                int byteArraySize = maxReadByteAtOnce;
                if(length - readBytes <= maxReadByteAtOnce) {
                    byteArraySize = length - readBytes;
                    System.out.println("length - readBytes <= maxReadByteAtOnce");
                    System.out.println("setting byteArraySize = " + byteArraySize);
                }
                byte[] newByteArr = new byte[byteArraySize];
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

    private void saveAudio(byte[] bytes, String name) throws IOException {
        String path = ConfigurationManager.getInstance().getProperty(ConfConstants.FILE_PATH) + File.separatorChar + name + ".mp3";
        FileUtils.writeByteArrayToFile(new File(path), bytes);
        SoundBoardServerThread.getInstance().notifyClientsAboutNewSound(this, name);
    }

    private void runAudioRequest(byte[] bytes) throws IOException {
        String name = new String(bytes, StandardCharsets.UTF_8).trim();
        System.out.println(name);

        String path = ConfigurationManager.getInstance().getProperty(ConfConstants.FILE_PATH) + File.separatorChar + name + ".mp3";
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] sendData = new byte[(int)file.length()];
        fileInputStream.read(sendData);

        SoundBoardServerThread.getInstance().notifySoundRequest(this, sendData);
    }


    public void notifyClientToPlayAudio(byte[] mp3Data) {
        try {
            byte[] lengthValueData = new byte[mp3Data.length + 4 + 1];
            ByteBuffer byteBuffer = ByteBuffer.wrap(lengthValueData);
            byte[] typeByte = new byte[] {48};
            byteBuffer.put(typeByte);
            byteBuffer.put(ByteBuffer.allocate(4).putInt(mp3Data.length).array());
            byteBuffer.put(mp3Data);
            socket.getOutputStream().write(byteBuffer.array());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifyClientAboutNewSound(String soundName) {
        try{
            byte[] soundNameBytes = soundName.getBytes(StandardCharsets.UTF_8);
            byte[] lengthValueData = new byte[soundNameBytes.length + 4 + 1];
            byte[] typeByte = new byte[] {49};
            ByteBuffer byteBuffer = ByteBuffer.wrap(lengthValueData);
            byteBuffer.put(typeByte);
            byteBuffer.put(ByteBuffer.allocate(4).putInt(soundNameBytes.length).array());
            byteBuffer.put(soundNameBytes);
            socket.getOutputStream().write(byteBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
