package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.config.StaticConfig;
import de.beckerdd.bennet.minecraft.technicserver.config.UserConfig;
import de.beckerdd.bennet.minecraft.technicserver.technic.Modpack;
import de.beckerdd.bennet.minecraft.technicserver.util.ANSIColor;
import de.beckerdd.bennet.minecraft.technicserver.util.ClientMod;
import de.beckerdd.bennet.minecraft.technicserver.util.ForgeInstaller;
import de.beckerdd.bennet.minecraft.technicserver.util.Logging;
import org.apache.commons.io.FileUtils;
import org.piwik.java.tracking.CustomVariable;
import org.piwik.java.tracking.PiwikRequest;
import org.piwik.java.tracking.PiwikTracker;

import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


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
 * Controller for the TechnicAPI / Technic Platform
 */
public class TechnicAPI {
    /**
     * The API contained Modpack
     */
    private Modpack modpack;

    /**
     * Read the API by it's URL
     * @param apiUrl API Url to the Modpack. Must be a http://api.technicpack.net/modpack/... Link
     * @throws IOException URL is unreachable
     * @throws MalformedURLException URL is Malformed
     */
    public TechnicAPI(String apiUrl) throws IOException {
        File state;

        URL url = new URL(apiUrl + "?build=349");
        Logging.log("Starting parse " + url);

        URLConnection conn = url.openConnection();
        if (!conn.getContentType().startsWith("application/json")) {
            throw new MalformedURLException("Not an API URL");
        }

        if (!(state = new File("modpack.state")).exists()) {
            Logging.logDebug("No State File Found");
            modpack = new Modpack(conn.getInputStream());
        }else{
            Logging.logDebug("Loading old State");
            try {
                FileInputStream fis = new FileInputStream(state);
                ObjectInputStream ois = new ObjectInputStream(fis);
                modpack = (Modpack) ois.readObject();

                ois.close();
                fis.close();

                modpack.update(conn.getInputStream());
            } catch (Exception ignore) {
                Logging.logErr("STATE FILE INVALID! PLEASE RESTART!");
                FileUtils.forceDelete(state);
                System.exit(1);
            }
        }
    }

    /**
     * Report Statisics to Piwik instance
     * @param runtime Loadup time
     */
    public void runAnalytics(String runtime) {
        runAnalytics(runtime,false);
    }

    /**
     * Report Statisics to Piwik instance
     * @param runtime Loadup time
     * @param force override user config
     */
    public void runAnalytics(String runtime, boolean force) {
        if (!UserConfig.isDisableAnalytics() || force) {
            PiwikRequest piwikRequest;

            try {
                piwikRequest = new PiwikRequest(2, new URL(StaticConfig.PIWIK_PROPERTY));
            } catch (MalformedURLException e) {
                return;
            }
            PiwikTracker piwikTracker = new PiwikTracker(StaticConfig.PIWIK_URL);

            if (Main.class.getPackage().getImplementationVersion() != null) {
                piwikRequest.addCustomTrackingParameter("implementation-version", Main.class.getPackage().getImplementationVersion());
                piwikRequest.setPageCustomVariable(new CustomVariable("implementation-version", Main.class.getPackage().getImplementationVersion()), 1);
            } else {
                if (System.getenv("TRAVIS") != null && System.getenv("TRAVIS").equalsIgnoreCase("true")) {
                    piwikRequest.addCustomTrackingParameter("implementation-version",
                            "TRAVIS #" + System.getenv("TRAVIS_BUILD_NUMBER") +
                                    " @ " + System.getenv("TRAVIS_COMMIT"));

                    piwikRequest.setPageCustomVariable(new CustomVariable("implementation-version", "TRAVIS"), 1);
                }else {
                    piwikRequest.addCustomTrackingParameter("implementation-version", "DEBUG-RUN");
                    piwikRequest.setPageCustomVariable(new CustomVariable("implementation-version", "DEBUG-RUN"), 1);
                }
            }

            piwikRequest.addCustomTrackingParameter("modpack-url", UserConfig.getUrl());
            piwikRequest.setPageCustomVariable(new CustomVariable("modpack-url", UserConfig.getUrl()), 2);
            piwikRequest.addCustomTrackingParameter("modpack-desired-version", UserConfig.getBuild());
            piwikRequest.setPageCustomVariable(new CustomVariable("modpack-desired-version", UserConfig.getBuild()), 3);
            piwikRequest.addCustomTrackingParameter("modpack-autoupdate", UserConfig.isAutoupdate());
            piwikRequest.setPageCustomVariable(new CustomVariable("modpack-autoupdate", UserConfig.isAutoupdate() ? "true" : "false"), 4);
            piwikRequest.addCustomTrackingParameter("modpack-name", modpack.getDisplayName());
            piwikRequest.setPageCustomVariable(new CustomVariable("modpack-name", modpack.getDisplayName()), 5);
            piwikRequest.addCustomTrackingParameter("modpack-setuptime", runtime);
            piwikRequest.setPageCustomVariable(new CustomVariable("modpack-setuptime", runtime), 6);

            try {
                piwikTracker.sendRequest(piwikRequest);
            } catch (IOException ignore) {  }
        }
    }

    /**
     * Try to Install the Modpack
     * @throws IOException installation failed at some Point
     */
    public void downloadAndInstallModpack() throws IOException {
        modpack.downloadAll().extractAll();
        try {
            Logging.log("Downloading Server Icon");
            modpack.getIcon().download("server-icon.png");
        } catch (MalformedURLException ignore) {
            Logging.logErr("Icon Download Failed");
        }
    }

    /**
     * Convert the Client Modpack to a Server Compatible one, by installing the Forge ModLoader
     * @throws IOException Failed Downloading Forge or moving modpack.jar
     */
    public void convertServer() throws IOException {
        URL url = new URL(
                "jar:file:" + Paths.get("").toAbsolutePath() + "/bin/modpack.jar!/version.json");
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        JarFile jarFile = conn.getJarFile();
        JarEntry jarEntry = conn.getJarEntry();

        ForgeInstaller.installForge(jarFile.getInputStream(jarEntry));
        FileUtils.copyFile(new File("bin/modpack.jar"), new File("modpack.jar"));
        modpack.getMinecraft().download();

        Logging.log("Forge Mod Loader successfully installed");
    }

    /**
     * Delete Client-Only Mods which may cause trouble with the Server
     */
    public void cleanClientMods() {
        try {
            for (File child : new File("mods/").listFiles()) {
                if (ClientMod.isClientMod(child.getName())) {
                    Logging.log("Deleting Clientmod-File " + child.getName());
                    if (child.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(child);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        child.delete();
                    }
                }
            }
        } catch (NullPointerException ignore) {
            Logging.logErr("Ohh, your seams mods folder empty o.Ô");
        }
    }

    /**
     * modpack getter
     * @return modpack
     */
    public Modpack getModpack() {
        return modpack;
    }

    /**
     * Save the current state of the Modpack by Searlizing it.
     * HACK
     * @throws IOException Serailization Failed :(
     */
    public void saveState() throws IOException {
        File stateDB = new File("modpack.state");
        FileOutputStream fos = new FileOutputStream(stateDB);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(modpack);
        oos.flush();
        oos.close();
        fos.close();
    }

    /**
     * Check for Modpack update
     * @param force override user config
     * @throws IOException Download, Extract or Convert failed
     * @throws InterruptedException sleep failed
     */
    public void updatePack(boolean force) throws IOException, InterruptedException {
        if (UserConfig.isAutoupdate() || force ||
                this.getModpack().getState() == Modpack.State.NOT_INSTALLED ) {
            this.downloadAndInstallModpack();
            this.convertServer();
            this.cleanClientMods();
        } else {
            Logging.log(ANSIColor.ANSI_RED + ANSIColor.ANSI_YELLOW_BACKGROUND +
                    "MODPACK UPDATE AVAILABLE, BUT AUTOUPDATE IS DISABLED. " +
                    "START WITH \"update\" AS PARAMETER OR SET AUTOUPDATE TO \"yes\"" +
                    ANSIColor.ANSI_RESET);

            Logging.logDebug("sleeping 5 sec.");
            Thread.sleep(5000);
        }
    }
}
