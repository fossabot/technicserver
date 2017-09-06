package de.beckerdd.bennet.minecraft.technicserver.technic;

import de.beckerdd.bennet.minecraft.technicserver.config.UserConfig;
import de.beckerdd.bennet.minecraft.technicserver.util.Downloader;
import de.beckerdd.bennet.minecraft.technicserver.util.Extractor;
import de.beckerdd.bennet.minecraft.technicserver.util.Logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;


/*
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
 * Class representing the Modpack inside the API.
 */
@SuppressWarnings("UnusedReturnValue")
public class Modpack implements Serializable {
  /**
   * Serialization UID.
   */
  public static final long serialVersionUID = 201709062240L;
  //region private Fields - API
  /**
   * Modpack ID.
   */
  private int id;
  /**
   * friendly name for Modpack.
   */
  private String name;
  /**
   * Modpacks Display Name.
   */
  private String displayName;
  /**
   * Username of the Modpack Creator.
   */
  private String user;
  /**
   * Download URL.
   */
  private URL url;
  /**
   * Minecraft Version.
   */
  private MinecraftVerion minecraft;
  /**
   * Modpack Version.
   */
  private String version;
  /**
   * Icon Resource.
   */
  private Resource icon;
  /**
   * Logo Resource.
   */
  private Resource logo;
  /**
   * Solder.
   */
  private Solder solder;
  //endregion

  //region private Field - Pack
  /**
   * Contained Mods.
   */
  private Set<Mod> mods;
  /**
   * Files from Mod ZIP-Files.
   */
  private Map<String, Set<String>> modFiles;
  //endregion

  /**
   * Current Installationstate.
   */
  private State state;
  /**
   * Current installed Version.
   */
  private String buildInstalled;

  /**
   * Parse Modpack from JSON Stream.
   * @param jsonStream API JSON to Parse
   * @throws IOException Parsing Failed
   */
  public Modpack(InputStream jsonStream) throws IOException {
    state = State.NOT_INSTALLED;
    Logging.log("Initializing Modpack from JSON InputStream");
    JsonReader rdr = Json.createReader(jsonStream);
    JsonObject obj = rdr.readObject();

    initSome(obj);
    if (obj.getString("solder", "").equals("")) {
      //solder = null;
      //mods = null;
      Logging.logDebug("Modpack isn't using Solder API");
    } else {
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

    if (!obj.getString("url", "").equals("")) {
      url = new URL(obj.getString("url"));
    }

    minecraft = new MinecraftVerion(obj.getString("minecraft"));
    version = obj.getString("version");
    icon = new Resource(obj.getJsonObject("icon"));
    logo = new Resource(obj.getJsonObject("logo"));
  }

  //region Getters

  /**
   * name getter.
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * minecraft getter.
   * @return minecraft
   */
  public MinecraftVerion getMinecraft() {
    return minecraft;
  }

  /**
   * icon getter.
   * @return icon
   */
  public Resource getIcon() {
    return icon;
  }

  /**
   * id getter.
   * @return id
   */
  public int getId() {
    return id;
  }

  /**
   * display_name getter.
   * @return display_name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * user getter.
   * @return user
   */
  public String getUser() {
    return user;
  }

  /**
   * url getter.
   * @return url
   */
  public URL getUrl() {
    return url;
  }

  /**
   * version getter.
   * @return version
   */
  public String getVersion() {
    return version;
  }

  /**
   * modfiles getter.
   * @return modfiles
   */
  public Map<String, Set<String>> getModFiles() {
    return modFiles;
  }

  /**
   * state getter.
   * @return state
   */
  public State getState() {
    return state;
  }

  /**
   * buildInstalled getter.
   * @return buildInstalled
   */
  public String getBuildInstalled() {
    return buildInstalled;
  }

  /**
   * logo getter.
   * @return logo
   */
  public Resource getLogo() {
    return logo;
  }

  /**
   * solder getter.
   * @return solder
   */
  public Solder getSolder() {
    return solder;
  }

  /**
   * mods getter.
   * @return mods
   */
  public Set<Mod> getMods() {
    return mods;
  }

  //endregion

  /**
   * override the minecraft version.
   * @param minecraft Minecraft Version-String
   */
  public void overrideMinecraft(String minecraft) {
    this.minecraft = new MinecraftVerion(minecraft);
  }

  /**
   * Add a mod to the internal List.
   * @param mod Mod to add
   * @return this #SyntacticSugar
   */
  public Modpack addMod(Mod mod) {
    mods.add(mod);
    return this;
  }

  /**
   * Download all Modpack files (Mods).
   * @return this
   * @throws IOException Download Failed due to source or destination unavailability
   */
  public Modpack downloadAll() throws IOException {
    FileUtils.forceMkdir(new File("cache/"));

    if (solder == null) {
      if (state == State.INSTALLED_UPDATEABLE) {
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
    } else {
      downloadAllViaSolder();
    }
    return this;
  }

  /**
   * Download the Modpack via the Solder API.
   * @throws IOException Download Failed due to source or destination unavailability
   */
  private void downloadAllViaSolder() throws IOException {
    HashSet<Mod> modsToDownload;
    if (state == State.INSTALLED_UPDATEABLE) {
      modsToDownload = new HashSet<>();

      mods = new HashSet<>();
      solder.initMods(this);

      HashSet<Mod> modsToClear = new HashSet<>();
      HashSet<Mod> oldMods = new HashSet<>(mods);

      Logging.logDebug("Searching for updated or removed mods");

      // find updated mods, we need to download
      modsToDownload.addAll(oldMods.stream().filter(
          oldMod -> mods.stream().anyMatch(
              mod -> oldMod.getName().equals(mod.getName())
                  && !oldMod.getVersion().equals(mod.getVersion()))).collect(Collectors.toSet()));
      // we need to clear updated mods too
      modsToClear.addAll(modsToDownload);

      // find mods removed, as they need to be cleared (obviously)
      modsToClear.addAll(oldMods.stream().filter(
          oldMod -> mods.stream().noneMatch(
              mod -> oldMod.getName().equals(mod.getName())
          )).collect(Collectors.toSet()));

      // obviously we also need to download newly added mods
      modsToDownload.addAll(mods.stream().filter(
          mod -> oldMods.stream().noneMatch(
              oldMod -> oldMod.getName().equals(mod.getName())
          )).collect(Collectors.toSet()));

      Logging.logDebug("Found " + modsToClear.size() + " updated or removed mods");
      Logging.logDebug("Starting to clean mod for update");

      modsToClear.parallelStream().forEach(modToClear -> {
        modFiles.get(modToClear.getName()).parallelStream().forEach(file -> {
          try {
            Logging.logDebug("Deleting " + file);
            FileUtils.forceDelete(new File(file));
          } catch (IOException e) {
            e.printStackTrace();
            Logging.logErr("could not delete " + file + " from mod " + modToClear.getName());
          }
        });
        // we ne to remove the mod from the fileset,
        // as it not part of the installation anymore
        modFiles.remove(modToClear.getName());
      });
    } else {
      modsToDownload = new HashSet<>(mods);
    }

    modsToDownload.parallelStream().forEach(mod -> {
      try {
        mod.download();
      } catch (IOException e) {
        e.printStackTrace();
        Logging.logErr("Could not download mod " + mod.getName());
      }
    });
    buildInstalled = solder.parseBuild(UserConfig.getBuild(), this);
  }

  /**
   * Extract all downloaded Mod ZIPs. Must be called after {@link #downloadAll()}
   * @return this
   * @throws IOException Source File or Destination Path are unreadable/writeable
   */
  public Modpack extractAll() throws IOException {
    if (solder == null) {
      modFiles.put("package", Extractor.extractZip("cache/package.zip"));
    } else {
      mods.parallelStream().forEach(mod -> {
        try {
          mod.extract();
        } catch (IOException e) {
          e.printStackTrace();
          Logging.logErr("Failed unpacking " + mod.getName());
        }
        modFiles.put(mod.getName(), mod.getFileSet());
      });
      if (state == State.INSTALLED_UPDATEABLE) {
        mods = new HashSet<>();
        solder.initMods(this);
      }
    }
    state = State.INSTALLED_UPTODATE;
    return this;
  }

  /**
   * Check and Update the internal State.
   * Called by TechnicApi#main(String[]) when modpack.state file found
   * @param jsonStream Current JSOn from the API
   * @throws IOException fired by Malformed URLs
   */
  public void update(InputStream jsonStream) throws IOException {
    if (state == null || state == State.NOT_INSTALLED) {
      throw new IllegalStateException();
    }

    initSome(Json.createReader(jsonStream).readObject());
    if ((solder == null && !buildInstalled.equals(version))
        || (solder != null
          && !buildInstalled.equals(solder.parseBuild(UserConfig.getBuild(), this)))) {
      state = State.INSTALLED_UPDATEABLE;
    } else {
      state = State.INSTALLED_UPTODATE;
    }
  }

  /**
   * Internal State Hack :).
   */
  public enum State implements Serializable {
    NOT_INSTALLED,
    INSTALLED_UPTODATE,
    INSTALLED_UPDATEABLE;

    public static final long serialVersionUID = 201709051908L;
  }
}
