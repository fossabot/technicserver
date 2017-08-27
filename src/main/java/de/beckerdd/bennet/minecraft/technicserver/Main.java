package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;
import org.piwik.java.tracking.CustomVariable;
import org.piwik.java.tracking.PiwikRequest;
import org.piwik.java.tracking.PiwikTracker;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


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

/**
 * Main Class. Containing the Programs entry point
 */
public class Main {

    /**
     * Main entry Point.
     * @param args Commandline Arguments
     */
    public static void main(String[] args) {

        Logging.plainPrintln("technicserver  Copyright (C) 2017  Bennet Becker <bennet@becker-dd.de>\n" +
                "This program comes with ABSOLUTELY NO WARRANTY;\n" +
                "This is free software, and you are welcome to redistribute it under certain conditions;\n\n" +
                "See https://github.com/bennet0496/technicserver for more details\n\n");

        try{
            if(!(new File("modpack.properties")).exists()){
                Properties properties = new Properties();
                properties.setProperty("url", "");
                properties.setProperty("build", "latest");
                properties.setProperty("autoupdate", "no");
                properties.setProperty("javaArgs", "-server -Xmx4G -XX:+DisableExplicitGC -XX:+AggressiveOpts");
                properties.store(new FileOutputStream("modpack.properties"), null);

                Logging.logErr("No Modpack configured! Please edit modpack.properties and restart.");
                System.exit(1);
            }

            if(Config.getUrl().equalsIgnoreCase("")){
                Logging.logErr("No Modpack configured! Please edit modpack.properties and restart.");
                System.exit(1);
            }

            PiwikRequest piwikRequest = new PiwikRequest(2, new URL("http://technicserver.minecraft.bennet.becker-dd.de/java"));
            PiwikTracker piwikTracker = new PiwikTracker("https://piwik.becker-dd.de/piwik.php");
            if(!Config.isDisableAnalytics()) {
                if (Main.class.getPackage().getImplementationVersion() != null) {
                    piwikRequest.addCustomTrackingParameter("implementation-version", Main.class.getPackage().getImplementationVersion());
                    piwikRequest.setPageCustomVariable(new CustomVariable("implementation-version", Main.class.getPackage().getImplementationVersion()), 1);
                } else {
                    piwikRequest.addCustomTrackingParameter("implementation-version", "DEBUG-RUN");
                    piwikRequest.setPageCustomVariable(new CustomVariable("implementation-version", "DEBUG-RUN"), 1);
                }
                piwikRequest.addCustomTrackingParameter("modpack-url", Config.getUrl());
                piwikRequest.setPageCustomVariable(new CustomVariable("modpack-url", Config.getUrl()), 2);
                piwikRequest.addCustomTrackingParameter("modpack-desired-version", Config.getBuild());
                piwikRequest.setPageCustomVariable(new CustomVariable("modpack-desired-version", Config.getBuild()), 3);
                piwikRequest.addCustomTrackingParameter("modpack-autoupdate", Config.isAutoupdate());
                piwikRequest.setPageCustomVariable(new CustomVariable("modpack-autoupdate", Config.isAutoupdate() ? "true" : "false"), 4);
            }
            Date start = new Date();

            TechnicAPI technicAPI = new TechnicAPI(Config.getUrl());

            if(technicAPI.getModpack().getState() != Modpack.State.INSTALLED_UPTODATE) {
                if(Config.isAutoupdate() ||
                        Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("update"))||
                        technicAPI.getModpack().getState() == Modpack.State.NOT_INSTALLED){
                    technicAPI.downloadAndInstallModpack();
                    technicAPI.convertServer();
                    technicAPI.cleanClientMods();
                }else{
                    Logging.log(Logging.ANSI_RED + Logging.ANSI_YELLOW_BACKGROUND + "MODPACK UPDATE AVAILABLE, BUT AUTOUPDATE IS DISABLED. START WITH \"update\" AS PARAMETER OR SET AUTOUPDATE TO \"yes\"" + Logging.ANSI_RESET);
                    Logging.logDebug("sleeping 5 sec.");
                    Thread.sleep(5000);
                }
            }else{
                Logging.log("Modpack is upto-date");
            }

            technicAPI.saveState();

            if(!Config.isDisableAnalytics()) {
                double end = (start.getTime() - (new Date()).getTime()) / 1000 % 60;
                piwikRequest.addCustomTrackingParameter("modpack-name", technicAPI.getModpack().getDisplayName());
                piwikRequest.setPageCustomVariable(new CustomVariable("modpack-name", technicAPI.getModpack().getDisplayName()), 5);
                piwikRequest.addCustomTrackingParameter("modpack-setuptime", Double.toString(end));
                piwikRequest.setPageCustomVariable(new CustomVariable("modpack-setuptime", Double.toString(end)), 6);

                piwikTracker.sendRequest(piwikRequest);
            }


            ArrayList<String> modpackStartArguments = new ArrayList<>();
            modpackStartArguments.add("java");
            modpackStartArguments.addAll(Config.getJavaArgs());
            modpackStartArguments.add("-jar");
            modpackStartArguments.add("modpack.jar");
            modpackStartArguments.add("nogui");

            ProcessBuilder modpackProcessTpl = new ProcessBuilder(modpackStartArguments);
            modpackProcessTpl.redirectErrorStream(true);
            modpackProcessTpl.directory(new File("."));
            modpackProcessTpl.inheritIO();

            Process modpackProcess = modpackProcessTpl.start();
            modpackProcess.waitFor();

        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            try {
                Logging.logErr(sw.toString());
            }catch (NullPointerException ignore){
                e.printStackTrace();
            }
        }

    }
}