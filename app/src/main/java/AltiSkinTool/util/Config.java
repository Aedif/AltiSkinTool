package AltiSkinTool.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Config {
    public static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + ".altiskintool" + File.separator;
    }

    public static File getConfigFile() {
        File config = new File(getUserDataDirectory() + File.separator + "config.properties");

        if (!config.exists()) {
            config.getParentFile().mkdirs();
            try (OutputStream output = new FileOutputStream(config)) {
                new Properties().store(output, null);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        return config;
    }

    public static Properties getProperties() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(getConfigFile())) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    public static void saveProperties(Properties prop) {
        try (OutputStream output = new FileOutputStream(getConfigFile())) {
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
