/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package mrfast.sbf.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.nbt.NBTTagCompound;

/*
 * Taken and modified from NotEnoughUpdates
 * https://github.com/NotEnoughUpdates/NotEnoughUpdates/
 */
public class AuctionUtil {
    private static final Gson gson = new Gson();

    public static JsonObject getApiGZIPSync(String urlS) throws IOException {
        URL url = new URL(urlS);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        String response = IOUtils.toString(new GZIPInputStream(connection.getInputStream()), StandardCharsets.UTF_8);

        return gson.fromJson(response, JsonObject.class);
    }

    private final static ExecutorService es = Executors.newFixedThreadPool(3);
    public static void getMyApiGZIPAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiGZIPSync(urlS));
			} catch (Exception e) {
				error.run();
			}
		});
	}

    public static String getInternalNameFromNBT(NBTTagCompound tag) {
        String internalName = null;
        if(tag != null && tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

            if(ea.hasKey("id", 8)) {
                internalName = ea.getString("id").replaceAll(":", "-");
            } else {
                return null;
            }

            if("PET".equals(internalName)) {
                String petInfo = ea.getString("petInfo");
                if(!petInfo.isEmpty()) {
                    JsonObject petInfoObject = gson.fromJson(petInfo, JsonObject.class);
                    internalName = petInfoObject.get("type").getAsString();
                    String tier = petInfoObject.get("tier").getAsString();
                    switch(tier) {
                        case "COMMON":
                            internalName += ";0"; break;
                        case "UNCOMMON":
                            internalName += ";1"; break;
                        case "RARE":
                            internalName += ";2"; break;
                        case "EPIC":
                            internalName += ";3"; break;
                        case "LEGENDARY":
                            internalName += ";4"; break;
                        case "MYTHIC":
                            internalName += ";5"; break;
                    }
                }
            }
            if("ENCHANTED_BOOK".equals(internalName)) {
                NBTTagCompound enchants = ea.getCompoundTag("enchantments");

                for(String enchant : enchants.getKeySet()) {
                    internalName = enchant.toUpperCase() + ";" + enchants.getInteger(enchant);
                    break;
                }
            }
        }

        return internalName;
    }

}
