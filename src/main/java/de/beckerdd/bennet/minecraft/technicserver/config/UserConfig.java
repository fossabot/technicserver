package de.beckerdd.bennet.minecraft.technicserver.config;

import de.beckerdd.bennet.minecraft.technicserver.util.Logging;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/*
 * Created by bennet on 8/7/17.
 *
 * technicserver - run modpacks from technicpack.net as server with ease.
 * Copyright (C) 2017 Bennet Becker <bennet@becker-dd.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singelton for Reading modpack.properties
 */
public final class UserConfig {
    /**
     * Singelton Instance
     */
    private static UserConfig instance = new UserConfig();
    /**
     * Parameter from propertiers File
     */
    private String url;
    /**
     * Parameter from propertiers File
     */
    private String build;
    /**
     * Parameter from propertiers File
     */
    private boolean autoupdate;
    /**
     * Parameter from propertiers File
     */
    private boolean disableAnalytics;
    /**
     * Parameter from propertiers File
     */
    private List<String> javaArgs;

    /**
     * Private Singelton Constructor. Reading File and setting up Singelton
     */
    private UserConfig(){
        FileInputStream input;
        Properties properties = new Properties();
        try {
            input = new FileInputStream("modpack.properties");
            properties.load(input);
        }catch (IOException io){
            io.printStackTrace();
            Logging.logErr("Failed to load UserConfig. Creating new!");
            properties.setProperty("url", "");
            properties.setProperty("build", "latest");
            properties.setProperty("autoupdate", "no");
            properties.setProperty("javaArgs", "-server -Xmx4G -XX:+DisableExplicitGC -XX:+AggressiveOpts");

            try {
                properties.store(new FileOutputStream("modpack.properties"), null);
            } catch (IOException e) {
                Logging.logErr("Failed read and writing config. caused by: ");
                e.printStackTrace();
                Logging.logErr("Make sure modpack.properties is readable and writeable and restart! EXITING!");
                System.exit(1);
            }
        }

            // load a properties file

            url = properties.getProperty("url");
            build = properties.getProperty("build");
            switch (properties.getProperty("autoupdate")){
                case "true":
                case "True":
                case "yes":
                case "Yes":
                case "1":
                    autoupdate = true;
                    break;
                default:
                    autoupdate = false;

            }
            switch (properties.getProperty("disableAnalytics", "false")){
                case "true":
                case "True":
                case "yes":
                case "Yes":
                case "1":
                    disableAnalytics = true;
                    break;
                default:
                    disableAnalytics = false;

            }
            javaArgs = Arrays.asList(properties.getProperty("javaArgs", "").split(" "));
    }

    /**
     * autoupdate property getter
     * @return autoupdate
     */
    public static boolean isAutoupdate() {
        return instance.autoupdate;
    }

    /**
     * build property getter
     * @return build
     */
    public static String getBuild() {
        return instance.build;
    }

    /**
     * url property getter
     * @return url
     */
    public static String getUrl() {
        return instance.url;
    }

    /**
     * javaArgs property getter
     * @return javaArgs
     */
    public static List<String> getJavaArgs() {
        return instance.javaArgs;
    }

    /**
     * disableAnalytics property getter
     * @return disableAnalytics
     */
    public static boolean isDisableAnalytics() {
        return instance.disableAnalytics;
    }

    /**
     * Reread UserConfig
     */
    public static void reload(){
        instance = new UserConfig();
    }
}
