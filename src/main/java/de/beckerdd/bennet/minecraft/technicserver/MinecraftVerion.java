package de.beckerdd.bennet.minecraft.technicserver;

import java.io.Serializable;
import java.lang.reflect.MalformedParametersException;
import java.util.regex.Pattern;

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
public class MinecraftVerion implements Serializable{
    private int major;
    private int minor;
    public MinecraftVerion(String versionString){
        String[] ver = versionString.split(Pattern.quote("."));
        if(ver.length > 3 || ver.length < 2){
            throw new MalformedParametersException("Invalid Minecraft Version String");
        }
        major = Integer.parseInt(ver[1]);
        try {
            minor = Integer.parseInt(ver[2]);
        }catch (ArrayIndexOutOfBoundsException e){
            minor = 0;
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override public String toString(){
        if(minor != 0)
            return "1." + major + "." + minor;
        else
            return "1." + major;
    }
}
