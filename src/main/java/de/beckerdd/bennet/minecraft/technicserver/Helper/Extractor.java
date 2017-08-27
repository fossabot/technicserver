package de.beckerdd.bennet.minecraft.technicserver.Helper;

import java.io.*;
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

/**
 * Static class for Extracting ZIP Files
 */
public class Extractor {
    /**
     * Extract a ZIP Stream
     * @param fis FileInputStream to the File
     * @return the Set of Files (not Folders) extracted
     * @throws IOException Input or Output not readable/writeable
     */
    public static HashSet<String> extractZip(FileInputStream fis) throws IOException {
        HashSet<String> files = new HashSet<>();
        int BUFFER = 2048;
        BufferedOutputStream dest = null;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
            Logging.log("Extracting: " + entry);
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            if(entry.isDirectory()){
                Logging.logDebug(entry + " is Directory");
                if(!(new File(entry.getName())).mkdir()){
                    Logging.logDebug("Directory exists");
                }
                continue;
            }
            FileOutputStream fos = new FileOutputStream(entry.getName());
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            if(entry.isDirectory())
                files.add(entry.getName());
        }
        zis.close();
        return files;
    }

    /**
     * Extract ZIP by Filename
     * @param filename Path to the ZIP File
     * @return the Set of Files (not Folders) extracted
     * @throws IOException Input or Output not readable/writeable
     */
    public static HashSet<String> extractZip(String filename) throws IOException {
        return extractZip(new FileInputStream(filename));
    }

}
