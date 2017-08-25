package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
public class Solder implements Serializable {
    private String url;

    private String mirror_url;

    public Solder(String solder) throws IOException {
        Logging.log("Install from Solder " + solder);
        if(solder.endsWith("/")) {
            url = solder.substring(0, solder.length() - 1);
        }else {
            url = solder;
        }

        URLConnection conn = (new URL(url)).openConnection();
        if(!conn.getContentType().startsWith("application/json")){
            throw new MalformedURLException("Not an API URL");
        }
        JsonReader rdr = Json.createReader(conn.getInputStream());
        JsonObject obj = rdr.readObject();

        Logging.log("Connecting to " + obj.getString("api") + " "
                + obj.getString("version") + "-" + obj.getString("stream"));

        conn = (new URL(url + "/modpack")).openConnection();

        if(!conn.getContentType().startsWith("application/json")){
            throw new MalformedURLException("Not an API URL");
        }
    }

    @Override
    public String toString(){
        return url;
    }

    public void initMods(Modpack modpack) throws IOException {
        try {
            String build = parseBuild(Config.getBuild(), modpack);
            URL modURL = new URL(url + "/modpack/" + modpack.getName() + "/" + build);
            URLConnection conn = modURL.openConnection();

            JsonReader rdr = Json.createReader(conn.getInputStream());
            JsonObject obj = rdr.readObject();

            if(!obj.getString("error", "none").equals("none")){
                throw new BuildNotFoundException("Build not Found");
            }
            modpack.overrideMinecraft(obj.getString("minecraft"));
            for (JsonValue j: obj.getJsonArray("mods")) {
                JsonObject o = (JsonObject)j;
                modpack.addMod(
                        new Mod(
                                o.getString("name"),
                                o.getString("version"),
                                o.getString("md5"),
                                Long.parseLong(o.getString("filesize")),
                                o.getString("url")));
            }
        } catch (MalformedURLException ignore){}
    }

    public String parseBuild(String build, Modpack modpack) throws IOException {
        URL modURL = new URL(url + "/modpack/" + modpack.getName());
        URLConnection conn = modURL.openConnection();

        JsonReader rdr = Json.createReader(conn.getInputStream());
        JsonObject obj = rdr.readObject();
        switch (build){
            case "latest":
                return obj.getString("latest");
            case "recommended":
                return obj.getString("recommended");
            default:
                return build;

        }
    }

    public String getUrl() {
        return url;
    }

    public String getMirror_url() {
        return mirror_url;
    }

    public static class BuildNotFoundException extends IOException{
        /**
         * Constructs an {@code BuildNotFoundException} with {@code null}
         * as its error detail message.
         */
        public BuildNotFoundException() {
            super();
        }

        /**
         * Constructs an {@code BuildNotFoundException} with the specified detail message.
         *
         * @param message
         *        The detail message (which is saved for later retrieval
         *        by the {@link #getMessage()} method)
         */
        public BuildNotFoundException(String message) {
            super(message);
        }

        /**
         * Constructs an {@code BuildNotFoundException} with the specified detail message
         * and cause.
         *
         * <p> Note that the detail message associated with {@code cause} is
         * <i>not</i> automatically incorporated into this exception's detail
         * message.
         *
         * @param message
         *        The detail message (which is saved for later retrieval
         *        by the {@link #getMessage()} method)
         *
         * @param cause
         *        The cause (which is saved for later retrieval by the
         *        {@link #getCause()} method).  (A null value is permitted,
         *        and indicates that the cause is nonexistent or unknown.)
         *
         * @since 1.6
         */
        public BuildNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs an {@code DownloadException} with the specified cause and a
         * detail message of {@code (cause==null ? null : cause.toString())}
         * (which typically contains the class and detail message of {@code cause}).
         * This constructor is useful for Download Exceptions that are little more
         * than wrappers for other throwables.
         *
         * @param cause
         *        The cause (which is saved for later retrieval by the
         *        {@link #getCause()} method).  (A null value is permitted,
         *        and indicates that the cause is nonexistent or unknown.)
         *
         * @since 1.6
         */
        public BuildNotFoundException(Throwable cause) {
            super(cause);
        }
    }
}
