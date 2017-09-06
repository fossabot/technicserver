package de.beckerdd.bennet.minecraft.technicserver.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * Singelton for Logging Purposes.
 */
public final class Logging {
  /**
   * Singelton Instance.
   */
  private static Logging instance = new Logging();
  /**
   * Parent Class Index in Stacktrace.
   */
  private static final int CALLING_CLASS_ORDER_IN_STACKTRACE = 2;

  /**
   * the Logfile.
   */
  private PrintWriter fileWriter;
  /**
   * Original Std Out form System.out - will be hijacked in CTOR
   */
  private final PrintStream stdOut = System.out;
  /**
   * Original Std Err form System.err - will be hijacked in CTOR
   */
  private final PrintStream stdErr = System.err;

  //private final String timezone = TimeZone.getDefault().getID();

  /**
   * Private Singelton Constructor, setting up Logging.
   */
  private Logging() {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

    File f = new File("logs/");

    if (!f.exists() || !f.isDirectory()) {
      f.mkdir();
    }
    String filename = "logs/technicserver-" + timestamp + ".log";
    try {
      fileWriter = new PrintWriter(filename, "UTF-8");
    } catch (IOException e) {
      System.err.println(e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(1);
    }
    //Hijack STDOUT and STDERR :)
    System.setOut(new LoggingStream(stdOut, false));
    System.setErr(new LoggingStream(stdOut, true));
  }

  private static void log(String logString, LoggingTag tag) {
    String caller = Thread.currentThread().getStackTrace()
        [CALLING_CLASS_ORDER_IN_STACKTRACE].getClassName();
    if (caller.equals(LoggingStream.class.getName())) {
      caller = Thread.currentThread().getStackTrace()
          [CALLING_CLASS_ORDER_IN_STACKTRACE + 1].getClassName();
    }
    String logme = String.format("[%s] [%s] [#%03d] [%s] %s",
        new Date(),
        tag.name(),
        Thread.currentThread().getId(),
        caller.substring(caller.lastIndexOf(".") + 1),  logString);

    if (tag == LoggingTag.ERR) {
      instance.stdErr.println(logme);
    }
    if (tag == LoggingTag.OUT || tag == LoggingTag.DBG) {
      instance.stdOut.println(logme);
    }
    instance.fileWriter.println(logme);
  }

  /**
   * Log a Message to STDOUT.
   * @param logString Log Message
   */
  public static void log(String logString) {
    log(logString, LoggingTag.OUT);
  }

  /**
   * Log a Message to STDERR.
   * @param logString Log Message
   */
  public static void logErr(String logString) {
    log(logString, LoggingTag.ERR);
  }

  /**
   * Log a Debug Message.
   * @param logString Log Message
   */
  public static void logDebug(String logString) {
    if (System.getProperty("DEBUG") != null) {
      log(logString, LoggingTag.DBG);
    }
  }

  /**
   * Kill the Logging Singelton -  can't undone.
   */
  public static void kill() {
    System.setOut(instance.stdOut);
    System.setErr(instance.stdErr);
    instance = null;
  }

  /**
   * Print a plain Message with out "decoration".
   * @param s String to print
   */
  public static void plainPrint(String s) {
    instance.stdOut.print(s);
  }

  /**
   * Print a plain Message with out "decoration" and a newline.
   * @param s String to print
   */
  public static void plainPrintln(String s) {
    instance.stdOut.println(s);
  }

  /**
   * Logging Tag
   */
  private enum  LoggingTag {
    OUT,
    ERR,
    DBG
  }
}
