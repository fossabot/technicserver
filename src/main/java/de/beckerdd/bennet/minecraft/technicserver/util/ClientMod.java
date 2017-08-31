package de.beckerdd.bennet.minecraft.technicserver.util;

import java.util.HashSet;
import java.util.Set;

/*
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
 * Static Class for detecting a "Client Mod" by it's ZIP Name
 */
public class ClientMod {
    /**
     * Set of Strings with Client Mod name Regualar Expressions
     */
    private static Set<String> clientModRegex = new HashSet<String>(){{
        add("^[lL]ite[lL]oader.*jar$");
        add("^.*litemod$");
        add("^WorldEdit$");
        add("^matmos$");
        add("^presencefootsteps$");
        add("^BetterFoliage.*jar$");
        add("^BetterFps.*jar$");
        add("^DynamicLights.*jar$");
        add("^GUI.*jar");
        add("^Opti[fF]ine.*jar$");
        add("^NotEnoughItems.*jar$");
        add("^Waila.*jar$");
        add("^SoundFilters.*jar$");
        add("^ShadersModCore-.*jar");
    }};

    /**
     * Check weather the Mod supplied by it's Name matches the known Client Mods
     * @param name Mod Zip Name
     * @return weather a Client mod was supplied
     */
    public static boolean isClientMod(String name){
        return clientModRegex.stream().anyMatch(name::matches);
    }
}
