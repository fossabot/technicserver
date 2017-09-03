package de.beckerdd.bennet.minecraft.technicserver.config;

/*
 * Created by Bennet on 31/08/2017 13:44.
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
 * Static Application Config
 */
public final class StaticConfig {
    /**
     * URL of the Piwik Server
     */
    public static String PIWIK_URL = "https://piwik.becker-dd.de/piwik.php";
    /**
     * Piwik Page URL
     */
    public static String PIWIK_PROPERTY = "http://technicserver.minecraft.bennet.becker-dd.de/java";

    /**
     * Minecraft Default Maven Repo for Forge installer
     */
    public static String LIBRARIES_URL = "https://libraries.minecraft.net/";

    /**
     * Minecraft Server JAR Download URL Pattern
     */
    public static String MINECRAFT_JAR_PATTERN = "https://s3.amazonaws.com/Minecraft.Download/versions/{MCVER}/minecraft_server.{MCVER}.jar";

    /**
     * Prevent initialization
     */
    private StaticConfig() {}
}
