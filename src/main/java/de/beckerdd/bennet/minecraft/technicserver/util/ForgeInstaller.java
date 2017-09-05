package de.beckerdd.bennet.minecraft.technicserver.util;

import de.beckerdd.bennet.minecraft.technicserver.config.StaticConfig;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Created by Bennet on 31/08/2017 15:40.
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
 * Class for installing the ForgeModloader Libraries
 */
public final class ForgeInstaller {
    /**
     * Prevent initialization
     */
    private ForgeInstaller() {}
    /**
     * Install Forge Modloader
     * @param inputStream InputSteam to contained version.json
     * @throws IOException Download failed!
     */
    public static void installForge(InputStream inputStream) throws IOException {
        JsonReader rdr = Json.createReader(inputStream);
        JsonObject obj = rdr.readObject();

        AtomicBoolean success = new AtomicBoolean(true);
        //for(JsonValue jVal : obj.getJsonArray("libraries")) {
        obj.getJsonArray("libraries").parallelStream().forEach( jVal -> {
            JsonObject lib = (JsonObject) jVal;

            //We wan't to skip unnecessary libs
            if (!lib.getBoolean("serverreq", false)) {
                return;
            }

            Logging.log("Start downloading Forge Library '" + lib.getString("name") + "'");
            String[] maven = lib.getString("name").split(":");
            maven[0] = maven[0].replace(".", "/");

            String pathname = String.join("/", maven);
            String filename = String.format("%s-%s.jar", maven[1], maven[2]);
            String baseurl = lib.getString("url", StaticConfig.LIBRARIES_URL);
            String url;
            if (baseurl.endsWith("/")) {
                url = String.format("%s%s/%s", lib.getString("url", StaticConfig.LIBRARIES_URL), pathname, filename);
            } else {
                url = String.format("%s/%s/%s", lib.getString("url", StaticConfig.LIBRARIES_URL), pathname, filename);
            }

            LinkedList<String> checksums = null;
            if (lib.getJsonArray("checksums") != null) {
                checksums = new LinkedList<>();
                for (JsonValue v : lib.getJsonArray("checksums")) {
                    checksums.add(v.toString());
                }
            }

            try {
                String downloadFile;
                try {
                    downloadFile = String.join("/", "libraries", pathname, filename);
                    Downloader.downloadFile(url, downloadFile);

                } catch (IOException ignore) {
                    downloadFile = String.join("/", "libraries", pathname, filename + ".pack.xz");
                    Downloader.downloadFile(url + ".pack.xz", downloadFile);
                    Extractor.extractXZ(downloadFile);
                    new File(downloadFile).renameTo(
                            new File(downloadFile.substring(0, downloadFile.lastIndexOf("."))));
                }
                /*if (checksums != null) {
                    String checksum = DigestUtils.sha1Hex(new FileInputStream(downloadFile));
                    if (checksums.stream().noneMatch(c -> c.equalsIgnoreCase(checksum))) {
                        //throw new Downloader.DownloadException("SHA1 Sum Missmatch");
                        Logging.logErr("SHA1 sum seams to missmatch for " + lib.getString("name") +
                                " expected one of {" + String.join(", ", checksums) + "}, got " +
                                DigestUtils.sha1Hex(new FileInputStream(downloadFile)));
                    }
                }*/
            } catch (IOException e) {
                success.set(false);
                Logging.logErr("Failed downloading Forge Library '" + lib.getString("name") + "'!");
                e.printStackTrace();
            }
        });

        if (!success.get()) {
            throw new IOException("ForgeInstaller Failed");
        }
    }
}
