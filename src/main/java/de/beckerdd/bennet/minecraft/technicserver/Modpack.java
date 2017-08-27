package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Extractor;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;
import org.apache.commons.io.FileUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bennet on 8/10/17.
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
 * Class representing the Modpack inside the API
 */
public class Modpack implements Serializable{
    //region private Fields - API
    private int id;
    private String name;
    private String displayName;
    private String user;
    private URL url;
    private String platformUrl;
    private MinecraftVerion minecraft;
    private int ratings;
    private int downloads;
    private int runs;
    private String description;
    private HashSet<String> tags;
    private boolean isServer;
    private boolean isOfficial;
    private String version;
    private boolean forceDir;
    //private List<Feed> feed;
    private Resource icon;
    private Resource logo;
    private Resource background;
    private Solder solder;
    private Discord discordServerId;
    //endregion

    //region private Field - Pack
    private HashSet<Mod> mods;
    private HashMap<String, HashSet<String>> modFiles;
    //endregion

    private State state;
    private String buildInstalled;

    /**
     * Parse Modpack from JSON Stream
     * @param jsonStream API JSON to Parse
     * @throws IOException Parsing Failed
     */
    public Modpack(InputStream jsonStream) throws IOException {
        state = State.NOT_INSTALLED;
        Logging.log("Initializing Modpack from JSON InputStream");
        JsonReader rdr = Json.createReader(jsonStream);
        JsonObject obj = rdr.readObject();

        initSome(obj);
        if(obj.getString("solder", "").equals("")){
            solder = null;
            mods = null;
            Logging.logDebug("Modpack isn't using Solder API");
        }else {
            solder = new Solder(obj.getString("solder"));
            mods = new HashSet<>();
            Logging.logDebug("Modpack is using Solder API @ " + solder);
            solder.initMods(this);
        }
        modFiles = new HashMap<>();

        Logging.log("Identified Modpack '" + displayName + "' (" + name + ") by " + user);
    }

    /**
     * Initializies internal fields. called by {@link #Modpack(InputStream)}
     * @param obj JSON object to parse from.
     * @throws MalformedURLException URLs inside JSON are Malformed
     */
    private void initSome(JsonObject obj) throws MalformedURLException {
        Logging.log("Initializing Modpack from JSON Object");

        id = obj.getInt("id");
        name = obj.getString("name");
        displayName = obj.getString("displayName");
        user = obj.getString("user");
        if(obj.getString("url", "").equals("")){
            url = null;
        }else{
            url = new URL(obj.getString("url"));
        }
        platformUrl = obj.getString("platformUrl");
        minecraft = new MinecraftVerion(obj.getString("minecraft"));
        ratings = obj.getInt("ratings");
        downloads = Integer.parseInt(obj.getString("downloads"));
        runs = Integer.parseInt(obj.getString("runs"));
        description = obj.getString("description");
        tags = new HashSet<>();
        tags.addAll(Arrays.asList(obj.getString("tags").split(",")));
        isServer = obj.getBoolean("isServer");
        isOfficial = obj.getBoolean("isOfficial");
        version = obj.getString("version");
        forceDir = obj.getBoolean("forceDir");
        //feed = null; //TODO
        icon = new Resource(obj.getJsonObject("icon"));
        logo = new Resource(obj.getJsonObject("logo"));
        background = new Resource(obj.getJsonObject("background"));
        if(obj.getString("discordServerId", "").equals("")){
            discordServerId = null;
        }else {
            discordServerId = new Discord(obj.getString("discordServerId"));
        }
    }

    //region Getters
    public String getName(){
        return name;
    }

    public MinecraftVerion getMinecraft() {
        return minecraft;
    }

    public Resource getIcon() {
        return icon;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUser() {
        return user;
    }

    public URL getUrl() {
        return url;
    }

    public String getPlatformUrl() {
        return platformUrl;
    }

    public int getRatings() {
        return ratings;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getRuns() {
        return runs;
    }

    public String getDescription() {
        return description;
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public boolean isServer() {
        return isServer;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public String getVersion() {
        return version;
    }

    public boolean isForceDir() {
        return forceDir;
    }

    /*public List<Feed> getFeed() {
        return feed;
    }*/

    public Resource getLogo() {
        return logo;
    }

    public Resource getBackground() {
        return background;
    }

    public Solder getSolder() {
        return solder;
    }

    public Discord getDiscordServerId() {
        return discordServerId;
    }

    public HashMap<String, HashSet<String>> getModFiles() {
        return modFiles;
    }

    public State getState() {
        return state;
    }

    public String getBuildInstalled() {
        return buildInstalled;
    }

    //endregion

    public void overrideMinecraft(String minecraft){
        this.minecraft = new MinecraftVerion(minecraft);
    }

    /**
     * Add a mod to the internal List
     * @param mod Mod to add
     * @return this #SyntacticSugar
     */
    public Modpack addMod(Mod mod) {
        mods.add(mod);
        return this;
    }

    /**
     * Download all Modpack files (Mods)
     * @return this
     * @throws IOException Download Failed due to source or destination unavailability
     */
    public Modpack downloadAll() throws IOException {
        FileUtils.forceMkdir(new File("cache/"));
        if(solder != null){
            if(state == State.INSTALLED_UPDATEABLE){
                HashSet<Mod> currentMods = new HashSet<>(mods);
                mods = new HashSet<>();
                solder.initMods(this);
                HashSet<Mod> updatedAndRemoved = new HashSet<>();
                HashSet<Mod> newMods = new HashSet<>();

                Logging.logDebug("Searching for updated or removed mods");
                //updated
                updatedAndRemoved.addAll(currentMods.stream().filter(
                        cm -> mods.stream().anyMatch(
                                nm -> cm.getName().equals(nm.getName()) &&
                                        !cm.getVersion().equals(nm.getVersion()))).collect(Collectors.toSet()));
                newMods.addAll(updatedAndRemoved);
                //removed
                updatedAndRemoved.addAll(currentMods.stream().filter(
                        cm -> mods.stream().allMatch(
                                nm -> !cm.getName().equals(nm.getName())
                )).collect(Collectors.toSet()));
                newMods.addAll(mods.stream().filter(
                        nm -> currentMods.stream().allMatch(
                                cm -> !cm.getName().equals(nm.getName())
                        )).collect(Collectors.toSet()));
                Logging.logDebug("Found " + updatedAndRemoved.size() + " updated or removed mods");
                Logging.logDebug("Starting to clean mod for update");
                updatedAndRemoved.forEach(m -> {
                    modFiles.get(m.getName()).forEach(fn -> {
                        try {
                            Logging.logDebug("Deleting " + fn);
                            FileUtils.forceDelete(new File(fn));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Logging.logErr("could not delete " + fn + " from mod " + m.getName());
                        }
                    });
                    try {
                        m.download();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logging.logErr("Could not download mod " + m.getName());
                    }
                });
                /*newMods.parallelStream().forEach(m -> {
                    try {
                        m.download();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logging.logErr("Could not download mod " + m.getName());
                    }
                });*/
                mods = new HashSet<>(newMods);
            }
            mods.parallelStream().forEach(mod -> {
                try {
                    mod.download();
                } catch (IOException e) {
                    e.printStackTrace();
                    Logging.logErr("Could not download mod " + mod.getName());
                }
            });
            buildInstalled = solder.parseBuild(Config.getBuild(), this);
        }else {
            if(state == State.INSTALLED_UPDATEABLE) {
                modFiles.get("package").parallelStream().forEach(fn -> {
                    try {
                        Logging.logDebug("Deleting " + fn);
                        FileUtils.forceDelete(new File(fn));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logging.logErr("could not delete " + fn);
                    }
                });
            }
            Downloader.downloadFile(url, "cache/package.zip");
            buildInstalled = version;
        }
        return this;
    }

    /**
     * Extract all downloaded Mod ZIPs. Must be called after {@link #downloadAll()}
     * @return this
     * @throws IOException Source File or Destination Path are unreadable/writeable
     */
    public Modpack extractAll() throws IOException {
        if(solder != null){
            mods.parallelStream().forEach(mod -> {
                try {
                    mod.extract();
                } catch (IOException e) {
                    e.printStackTrace();
                    Logging.logErr("Failed unpacking " + mod.getName());
                }
                modFiles.put(mod.getName(), mod.getFileSet());
            });
            if(state == State.INSTALLED_UPDATEABLE){
                mods = new HashSet<>();
                solder.initMods(this);
            }
        }else{
            modFiles.put("package", Extractor.extractZip("cache/package.zip"));
        }
        state = State.INSTALLED_UPTODATE;
        return this;
    }

    /**
     * Check and Update the internal State. Called by {@Link TechnicAPI#main(String[])} when modpack.state file found
     * @param jsonStream Current JSOn from the API
     * @throws IOException fired by Malformed URLs
     */
    public void update(InputStream jsonStream) throws IOException {
        if(state == null || state == State.NOT_INSTALLED)
            throw new IllegalStateException();

        initSome(Json.createReader(jsonStream).readObject());
        if(((solder == null) && !(buildInstalled.equals(version))) ||
                ((solder != null) && !buildInstalled.equals(solder.parseBuild(Config.getBuild(), this)))){
            state = State.INSTALLED_UPDATEABLE;
        }else{
            state = State.INSTALLED_UPTODATE;
        }
    }

    /**
     * Internal State Hack :)
     */
    public enum State implements Serializable{
        NOT_INSTALLED,
        INSTALLED_UPTODATE,
        INSTALLED_UPDATEABLE
    }
}
