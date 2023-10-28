import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {


        String test = "senin_anan";

        byte[] bytes = new byte[test.getBytes(StandardCharsets.UTF_8).length + 1 + test.length()];
        byte[] typeByte = new byte[] {48};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.put(typeByte);
        byteBuffer.put(ByteBuffer.allocate(4).putInt(test.length()).array());
        byteBuffer.put(test.getBytes(StandardCharsets.UTF_8));



        /////////
/*
        File file = new File("E:\\tests\\senin_anan.mp3");
        FileInputStream loc = new FileInputStream(file);
        byte[] sendData = new byte[(int)file.length()];
        loc.read(sendData);


        String name = "senin_anana";
        byte[] nameBytesArr = name.getBytes(Charset.forName("UTF-8"));
        byte[] nameBytes = new byte[12];
        System.arraycopy(nameBytesArr, 0, nameBytes, 0, nameBytesArr.length);

        byte[] typeByte = new byte[] {49};


        byte[] bytes = new byte[sendData.length + 4 + 1 + 12];  //1 data type, 4 data length, 12 audio name
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.put(typeByte);
        byteBuffer.put(ByteBuffer.allocate(4).putInt(sendData.length).array());
        byteBuffer.put(nameBytes);
        byteBuffer.put(sendData);

        System.out.println("sending " + byteBuffer.array().length + "bytes");
        System.out.println(Arrays.toString(ByteBuffer.allocate(4).putInt(sendData.length).array()));

        for (byte b : ByteBuffer.allocate(4).putInt(sendData.length).array()) {
            System.out.format("0x%x ", b);
        }
 */

        ////////////////
        Socket socket = new Socket("localhost", 3131);
        OutputStream output = socket.getOutputStream();


        output.write(byteBuffer.array());


        Thread thread = new Thread(() -> {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                while (true) {
                    int length = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();
                    byte[] mp3Data = readData(inputStream, length);
                    playAudio(mp3Data);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        thread.start();

        //playAudio();

        while (true) {

        }

    }

    private static byte[] readData(InputStream inputStream, int length) {
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

    private static void playAudio(byte[] audioResponse) {
        try {
            //FileInputStream fis = new FileInputStream("E:\\tests\\senin_anan.mp3");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(audioResponse);

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            AdvancedPlayer player = null;
            player = new AdvancedPlayer(bufferedInputStream);
            player.play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }
}
