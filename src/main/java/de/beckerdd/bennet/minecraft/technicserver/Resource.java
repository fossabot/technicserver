package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

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
public class Resource implements Serializable{
    private URL url;
    private String md5;

    public Resource(JsonObject icon) {
        try {
            url = new URL(icon.getString("url"));
            md5 = icon.getString("md5");
        } catch (MalformedURLException e) {
            Logging.logErr("Failed Creating Resource");
            Logging.logErr(e.getLocalizedMessage());
        }
    }

    public void download(String path) throws IOException {
        Downloader.downloadFile(url, path);
    }

    public URL getUrl() {
        return url;
    }

    public String getMd5() {
        return md5;
    }
}
