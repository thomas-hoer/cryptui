/*
 * Copyright 2017 thomas-hoer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cryptui.util;

import cryptui.ui.CryptUI;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserConfiguration {

    public static final String SERVER_KEY = "server";
    public static final String SELECTED_KEY = "selectedKey";

    private static final String SERVER_DEFAULT = "https://cryptui.de";

    private static Properties userProperties;
    private static String HOME_DIRECTORY;

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    private static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        getProperties().put(key, value);
        try (final FileWriter fileWriter = new FileWriter(getHomeDirectory() + File.separator + "config.properties");) {
            getProperties().store(fileWriter, "");
        } catch (IOException ex) {
            Logger.getLogger(UserConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Properties getProperties() {
        if (userProperties != null) {
            return userProperties;
        }
        userProperties = new Properties();
        try (final FileReader fileReader = new FileReader(getHomeDirectory() + File.separator + "config.properties");) {
            userProperties.load(fileReader);
        } catch (IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userProperties;
    }

    public static File getKeysDirectory() {
        String home = getHomeDirectory();
        File keyDir = new File(home + File.separator + "key");
        keyDir.mkdir();
        return keyDir;
    }

    private static String getHomeDirectory() {
        if (HOME_DIRECTORY != null) {
            return HOME_DIRECTORY;
        }

        String dir = System.getenv("LOCALAPPDATA");
        if (dir != null) {
            File file = new File(dir + File.separator + "cryptui");
            file.mkdir();
            if (file.exists()) {
                HOME_DIRECTORY = file.getAbsolutePath();
                return HOME_DIRECTORY;
            }
        }
        dir = System.getProperty("user.home");
        if (dir != null) {
            File file = new File(dir + File.separator + "cryptui");
            file.mkdir();
            if (file.exists()) {
                HOME_DIRECTORY = file.getAbsolutePath();
                return HOME_DIRECTORY;
            }
        }
        return null;
    }

    public static String getServer() {
        return getProperty(SERVER_KEY, SERVER_DEFAULT);
    }
}
