import conf.ConfigurationManager;
import thread.SoundBoardServerThread;

public class Main {
    public static void main(String[] args) {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        //System.out.println(configurationManager.getProperty("test.test"));

        SoundBoardServerThread soundBoardServerThread = SoundBoardServerThread.getInstance();
        Thread soundBoardThread = new Thread(soundBoardServerThread);
        soundBoardThread.start();
    }
}
