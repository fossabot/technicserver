package de.beckerdd.bennet.minecraft.technicserver;

import de.beckerdd.bennet.minecraft.technicserver.Helper.ClientMod;
import de.beckerdd.bennet.minecraft.technicserver.Helper.Logging;
import de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer.InstallerAction;
import de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer.ServerInstall;
import de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer.VersionInfo;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


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
public class TechnicAPI {
    private Modpack modpack;

    public TechnicAPI(String apiUrl) throws IOException {
        File state;

        URL url = new URL(apiUrl + "?build=349");
        Logging.log("Starting parse " + url);

        URLConnection conn = url.openConnection();
        if (!conn.getContentType().startsWith("application/json")) {
            throw new MalformedURLException("Not an API URL");
        }

        if(!(state = new File("modpack.state")).exists()) {
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
            }catch (Exception ignore){
                Logging.logErr("STATE FILE INVALID! PLEASE RESTART!");
                FileUtils.forceDelete(state);
                System.exit(1);
            }
        }
    }

    public void downloadAndInstallModpack() throws IOException {
        modpack.downloadAll().extractAll();
        try{
            Logging.log("Downloading Server Icon");
            modpack.getIcon().download("server-icon.png");
        }catch (MalformedURLException ignore){
            Logging.logErr("Icon Download Failed");
        }
    }

    public void convertServer() throws IOException {
        URL url = new URL(
                "jar:file:" + Paths.get("").toAbsolutePath() + "/bin/modpack.jar!/version.json");
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        JarFile jarFile = conn.getJarFile();
        JarEntry jarEntry = conn.getJarEntry();

        VersionInfo.overriddenMinecraftVersion = modpack.getMinecraft().toString();
        VersionInfo.LazyInit(jarFile.getInputStream(jarEntry));
        ServerInstall.headless = true;
        if (!InstallerAction.SERVER.run(new File("."), s -> true)) {
            Logging.logErr("There was an error during server installation");
        } else {
            Files.copy(
                    FileSystems.getDefault().getPath("bin","modpack.jar"),
                    FileSystems.getDefault().getPath("modpack.jar"), StandardCopyOption.REPLACE_EXISTING);
            Logging.log("Forge Mod Loader successfully installed");
        }
    }

    public void cleanClientMods() {
        for(File child : new File("mods/").listFiles()){
            if(ClientMod.isClientMod(child.getName())){
                Logging.log("Deleting Clientmod-File " + child.getName());
                if(child.isDirectory()){
                    try {
                        FileUtils.deleteDirectory(child);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    boolean b = child.delete();
                }
            }
        }
    }

    public Modpack getModpack() {
        return modpack;
    }

    public void saveState() throws IOException {
        File stateDB = new File("modpack.state");
        FileOutputStream fos = new FileOutputStream(stateDB);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(modpack);
        oos.flush();
        oos.close();
        fos.close();
    }
}
