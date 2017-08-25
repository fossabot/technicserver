package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
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
public class Config {
    private static Config instance = new Config();
    private String url;
    private String build;
    private boolean autoupdate;
    private boolean disableAnalytics;
    private List<String> javaArgs;

    private Config(){
        try {
            FileInputStream input = new FileInputStream("modpack.properties");

            // load a properties file
            Properties properties = new Properties();
            properties.load(input);
            url = properties.getProperty("url");
            build = properties.getProperty("build");
            switch (properties.getProperty("autoupdate")){
                case "true":
                case "True":
                case "yes":
                case "Yes":
                case "1":
                    autoupdate = true;
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
                default:
                    disableAnalytics = false;

            }
            javaArgs = Arrays.asList(properties.getProperty("javaArgs", "").split(" "));
        }catch (IOException io){
            io.printStackTrace();
            Logging.logErr("Failed to load Config. Exiting!");
            System.exit(1);
        }
    }

    public static boolean isAutoupdate() {
        return instance.autoupdate;
    }

    public static String getBuild() {
        return instance.build;
    }

    public static String getUrl() {
        return instance.url;
    }

    public static List<String> getJavaArgs() {
        return instance.javaArgs;
    }

    public static boolean isDisableAnalytics() {
        return instance.disableAnalytics;
    }

    public static void reload(){
        instance = new Config();
    }
}
