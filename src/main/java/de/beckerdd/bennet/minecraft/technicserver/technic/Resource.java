package de.beckerdd.bennet.minecraft.technicserver.technic;

import de.beckerdd.bennet.minecraft.technicserver.util.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.util.Logging;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.JsonObject;

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
 * Represents a Resource File.
 */
public class Resource implements Serializable {
  /**
   * File URL.
   */
  private URL url;
  /**
   * File Checksum.
   */
  private String md5;

  public static final long serialVersionUID = 201709051908L;

  /**
   * Parse the JSON Object to initializie the Resource.
   * @param jsonObject JsonObject to parse
   */
  public Resource(JsonObject jsonObject) {
    try {
      url = new URL(jsonObject.getString("url"));
      md5 = jsonObject.getString("md5");
    } catch (MalformedURLException e) {
      Logging.logErr("Failed Creating Resource");
      Logging.logErr(e.getLocalizedMessage());
    }
  }

  /**
   * Try to Download the Resource.
   * @param path Destination Path
   * @throws IOException Download Failed
   */
  public void download(String path) throws IOException {
    Downloader.downloadFile(url, path);
  }

  /**
   * url getter.
   * @return url
   */
  public URL getUrl() {
    return url;
  }

  /**
   * md5 getter.
   * @return md5
   */
  public String getMd5() {
    return md5;
  }
}
