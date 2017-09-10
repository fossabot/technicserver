package de.beckerdd.bennet.minecraft.technicserver.util;

import java.io.OutputStream;
import java.io.PrintStream;

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
 * Logging Stream for Hijacking STDOUT and STDERR.
 */
@SuppressWarnings("CanBeFinal")
public class LoggingStream extends PrintStream {

  /**
   * is the a Error Stream?.
   */
  private boolean isErr;

  /**
   * Setup the Stream.
   * @param out Stream
   * @param isErr Error?
   */
  public LoggingStream(OutputStream out, boolean isErr) {
    super(out);
    this.isErr = isErr;
  }

  /**
   * Redirect println to Logging Singelton.
   * @param line Line to Print
   */
  @Override public void println(String line) {
    if (isErr) {
      Logging.logErr(line);
    } else {
      Logging.log(line);
    }
  }
}
