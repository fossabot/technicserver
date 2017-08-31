package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.config.UserConfig;
import de.beckerdd.bennet.minecraft.technicserver.util.Logging;

import java.io.*;
import java.util.*;


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
            if(UserConfig.getUrl().equalsIgnoreCase("")){
                Logging.logErr("No Modpack configured! Please edit modpack.properties and restart.");
                System.exit(1);
            }

            Date startTime = new Date();

            TechnicAPI technicAPI = new TechnicAPI(UserConfig.getUrl());

            if(technicAPI.getModpack().getState() != Modpack.State.INSTALLED_UPTODATE) {
                technicAPI.updatePack(Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("update")));
            }else{
                Logging.log("Modpack is upto-date");
            }

            technicAPI.saveState();

            technicAPI.runAnalytics(Double.toString(
                            (startTime.getTime() - (new Date()).getTime()) / 1000 % 60
                    ));


            ArrayList<String> modpackStartArguments = new ArrayList<>();
            modpackStartArguments.add("java");
            modpackStartArguments.addAll(UserConfig.getJavaArgs());
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
            e.printStackTrace();
            System.exit(1);
        }
    }
}