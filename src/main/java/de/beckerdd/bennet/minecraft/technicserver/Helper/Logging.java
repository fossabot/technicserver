package de.beckerdd.bennet.minecraft.technicserver.Helper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

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
 * Singelton for Logging Purposes
 */
public class Logging {
    /**
     * Singelton Instance
     */
    private static Logging instance = new Logging();

    /**
     * the Logfile
     */
    private PrintWriter fileWriter;
    /**
     * Original Std Out form System.out - will be hijacked in CTOR
     */
    private PrintStream stdOut = System.out;
    /**
     * Original Std Err form System.err - will be hijacked in CTOR
     */
    private PrintStream stdErr = System.err;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    //private final String timezone = TimeZone.getDefault().getID();

    /**
     * Private Singelton Constructor, setting up Logging
     */
    private Logging(){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        File f = new File("logs/");

        if(!f.exists() || !f.isDirectory()){
            f.mkdir();
        }
        String filename = "logs/technicserver-" + timestamp + ".log";
        try {
            fileWriter = new PrintWriter(filename, "UTF-8");
        }catch (IOException e){
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }
        //Hijack STDOUT and STDERR :)
        System.setOut(new LoggingStream(stdOut, false));
        System.setErr(new LoggingStream(stdOut, true));
    }

    /**
     * Log a Message to STDOUT
     * @param logString Log Message
     */
    public static void log(String logString){
        String caller = Thread.currentThread().getStackTrace()[2].getClassName();
        if(caller.equals(LoggingStream.class.getName())){
            caller = Thread.currentThread().getStackTrace()[3].getClassName();
        }
        String logme = String.format("[%s] [OUT] [#%03d] [%s] %s",
                new Date(),
                Thread.currentThread().getId(),
                caller.substring(caller.lastIndexOf(".") + 1),  logString);
        instance.stdOut.println(logme);
        instance.fileWriter.println(logme);
    }

    /**
     * Log a Message to STDERR
     * @param logString Log Message
     */
    public static void logErr(String logString){
        String caller = Thread.currentThread().getStackTrace()[2].getClassName();
        if(caller.equals(LoggingStream.class.getName())){
            caller = Thread.currentThread().getStackTrace()[3].getClassName();
        }
        String logme = String.format("[%s] [ERR] [#%03d] [%s] %s",
                new Date(),
                Thread.currentThread().getId(),
                caller.substring(caller.lastIndexOf(".") + 1),  logString);
        instance.stdErr.println(logme);
        instance.fileWriter.println(logme);
    }

    /**
     * Log a Debug Message
     * @param logString Log Message
     */
    public static void logDebug(String logString) {
        String caller = Thread.currentThread().getStackTrace()[2].getClassName();
        if(caller.equals(LoggingStream.class.getName())){
            caller = Thread.currentThread().getStackTrace()[3].getClassName();
        }
        String logme = String.format("[%s] [DBG] [#%03d] [%s] %s",
                new Date(),
                Thread.currentThread().getId(),
                caller.substring(caller.lastIndexOf(".") + 1),  logString);
        if(System.getProperty("DEBUG") != null) {
            instance.stdOut.println(logme);
            instance.fileWriter.println(logme);
        }
    }

    /**
     * Kill the Logging Singelton -  can't undone
     */
    public static void kill() {
        System.setOut(instance.stdOut);
        System.setErr(instance.stdErr);
        instance = null;
    }

    /**
     * Print a plain Message with out "decoration"
     * @param s String to print
     */
    public static void plainPrint(String s) {
        instance.stdOut.print(s);
    }

    /**
     * Print a plain Message with out "decoration" and a newline
     * @param s String to print
     */
    public static void plainPrintln(String s) {
        instance.stdOut.println(s);
    }
}
