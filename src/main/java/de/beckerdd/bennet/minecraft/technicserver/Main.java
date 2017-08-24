package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Main {

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

            TechnicAPI technic = new TechnicAPI(Config.getUrl());
            if(technic.getModpack().getState() != Modpack.State.INSTALLED_UPTODATE) {
                if(Config.isAutoupdate() ||
                        Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("update"))||
                        technic.getModpack().getState() == Modpack.State.NOT_INSTALLED){
                    technic.downloadAndInstallModpack();
                    technic.convertServer();
                    technic.cleanClientMods();
                }else{
                    Logging.log(Logging.ANSI_RED + Logging.ANSI_YELLOW_BACKGROUND + "MODPACK UPDATE AVAILABLE, BUT AUTOUPDATE IS DISABLED. START WITH \"update\" AS PARAMETER OR SET AUTOUPDATE TO \"yes\"" + Logging.ANSI_RESET);
                    Logging.logDebug("sleeping 5 sec.");
                    Thread.sleep(5000);
                }
            }else{
                Logging.log("Modpack is upto-date");
            }
            technic.saveState();

            //Logging.kill();

            ArrayList<String> arguments = new ArrayList<>();
            arguments.add("java");
            //arguments.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
            arguments.addAll(Config.getJavaArgs());
            arguments.add("-jar");
            arguments.add("modpack.jar");
            //arguments.addAll(Arrays.asList(args));
            arguments.add("nogui");
            ProcessBuilder pb = new ProcessBuilder(arguments);
            pb.redirectErrorStream(true);
            pb.directory(new File("."));
            //System.out.println("Directory: " + pb.directory().getAbsolutePath());
            pb.inheritIO();
            Process p = pb.start();
            /*InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                System.out.println( line ); // Or just ignore it
            }*/
            p.waitFor();
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