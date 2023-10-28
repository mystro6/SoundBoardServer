package conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {

    private Properties properties = new Properties();
    private static ConfigurationManager configurationManager;

    public static ConfigurationManager getInstance(){
        if(configurationManager == null) {
            configurationManager = new ConfigurationManager();
        }

        return configurationManager;
    }
    private ConfigurationManager() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
