package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Extractor;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
public class Mod implements Serializable{
    private final String name;
    private final String version;
    private final String md5;
    private final long filesize;
    private final String url;

    private final HashSet<String> fileSet;

    private final String cacheFilename;

    public Mod(String name, String version, String md5, long filesize, String url) {
        this.name = name;
        this.version = version;
        this.md5 = md5;
        this.filesize = filesize;
        this.url = url;

        cacheFilename = "cache/" + name + "-" + version + ".zip";

        fileSet = new HashSet<>();
    }

    public Mod download() throws IOException {
        if(!(new File(cacheFilename)).exists() ||
                !DigestUtils.md5Hex(new FileInputStream(new File(cacheFilename))).equalsIgnoreCase(md5)) {
            URL url = new URL(this.url);

            Downloader.downloadFile(url, cacheFilename, md5);
        }else {
            Logging.log("File " + cacheFilename + " cached. Skipping.");
        }
        return this;
    }

    public Mod extract() throws IOException {
        fileSet.addAll(Extractor.extractZip(cacheFilename));
        return this;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public HashSet<String> getFileSet() {
        return fileSet;
    }
}
