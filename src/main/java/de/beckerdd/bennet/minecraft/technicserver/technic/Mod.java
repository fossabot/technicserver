package de.beckerdd.bennet.minecraft.technicserver.technic;

import de.beckerdd.bennet.minecraft.technicserver.util.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.util.Extractor;
import de.beckerdd.bennet.minecraft.technicserver.util.Logging;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;

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
 * Representation of a Mod inside an Solder API
 */
public class Mod implements Serializable {
    /**
     * Name of the Mod
     */
    private final String name;
    /**
     * Mod Version
     */
    private final String version;
    /**
     * Downloadfile MD5 sum
     */
    private final String md5;
    /**
     * Downloadfile size
     */
    private final long filesize;
    /**
     * Download url
     */
    private final String url;

    /**
     * All Files belonging to this particular Mod
     */
    private final HashSet<String> fileSet;

    /**
     * Filename in the cache dir
     */
    private final String cacheFilename;

    /**
     * Setup a mod by it's parameters
     * @param name Mod name
     * @param version Mod version
     * @param md5 Download File MD5
     * @param filesize Download File size
     * @param url Download URL
     */
    public Mod(String name, String version, String md5, long filesize, String url) {
        this.name = name;
        this.version = version;
        this.md5 = md5;
        this.filesize = filesize;
        this.url = url;

        cacheFilename = "cache/" + name + "-" + version + ".zip";

        fileSet = new HashSet<>();
    }

    /**
     * Try to Download the Mod
     * @return this instance #SyntacticSugar
     * @throws IOException Download Failed
     */
    public Mod download() throws IOException {
        if ((new File(cacheFilename)).exists() &&
                DigestUtils.md5Hex(new FileInputStream(new File(cacheFilename))).equalsIgnoreCase(md5)) {
                    Logging.log("File " + cacheFilename + " cached. Skipping.");
        } else {
            URL url = new URL(this.url);
            Downloader.downloadFile(url, cacheFilename, md5);
        }
        return this;
    }

    /**
     * Try to Extract the Downloaded ZIP File
     * @return this instance #SyntacticSugar
     * @throws IOException Extraction Failed
     */
    public Mod extract() throws IOException {
        fileSet.addAll(Extractor.extractZip(cacheFilename));
        return this;
    }

    /**
     * name getter
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * version getter
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * fileSet getter
     * @return fileSet
     */
    public HashSet<String> getFileSet() {
        return fileSet;
    }
}
