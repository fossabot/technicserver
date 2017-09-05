package de.beckerdd.bennet.minecraft.technicserver.technic;

import de.beckerdd.bennet.minecraft.technicserver.config.StaticConfig;
import de.beckerdd.bennet.minecraft.technicserver.util.Downloader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.MalformedParametersException;
import java.util.regex.Pattern;

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
 * Represent the Minecraft Version in very fancy manner
 */
public class MinecraftVerion implements Serializable {
    /**
     * Major Version part. E.g. 7 if MC Version is 1.7.X
     */
    private int major;
    /**
     * Minor Version part. E.g. 10 if MC Version is 1.7.10
     */
    private int minor;

    public static final long serialVersionUID = 201709051908L;

    /**
     * Setup this very fancy class
     * @param versionString original version string such as "1.7.10"
     */
    public MinecraftVerion(String versionString) {
        String[] ver = versionString.split(Pattern.quote("."));
        if (ver.length > 3 || ver.length < 2) {
            throw new MalformedParametersException("Invalid Minecraft Version String");
        }
        major = Integer.parseInt(ver[1]);
        try {
            minor = Integer.parseInt(ver[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
            minor = 0;
        }
    }

    /**
     * major getter
     * @return major
     */
    public int getMajor() {
        return major;
    }

    /**
     * minor getter
     * @return minor
     */
    public int getMinor() {
        return minor;
    }

    public void download() throws IOException {
        Downloader.downloadFile(
                StaticConfig.MINECRAFT_JAR_PATTERN.replace("{MCVER}", this.toString()),
                "minecraft_server" + toString() + ".jar");
    }

    /**
     * give back the "original" version String
     * @return version string
     */
    @Override public String toString() {
        return minor == 0 ? "1." + major : "1." + major + "." + minor;
    }
}
