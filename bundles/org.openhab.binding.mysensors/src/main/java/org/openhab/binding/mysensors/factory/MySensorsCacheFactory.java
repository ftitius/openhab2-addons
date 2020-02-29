/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.factory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Cache may be used to store information across restart of MySensors connection.
 *
 * @author Andrea Cioni - Initial contribution
 *         asdf asd
 */

public class MySensorsCacheFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String CACHE_BASE_PATH;
    private static final String CACHE_FILE_SUFFIX = ".cached";

    public static final String GIVEN_IDS_CACHE_FILE = "given_ids";

    private final Gson gson = new Gson();

    public MySensorsCacheFactory(String userDataFolder) {
        CACHE_BASE_PATH = userDataFolder + "/mysensors/cache";
        initializeCacheDir();
    }

    private void initializeCacheDir() {
        File f = new File(CACHE_BASE_PATH);
        if (!f.exists()) {
            logger.debug("Creating cache directory...");
            f.mkdirs();
        }
    }

    /**
     * Read the cache file.
     */
    public <T> T readCache(String cacheName, T defaulT, Type clasz) {
        return jsonFromFile(cacheName, defaulT, clasz);
    }

    /**
     * Write the cache file.
     */
    public <T> void writeCache(String cacheName, T obj, Type clasz) {
        jsonToFile(cacheName, obj, clasz);
    }

    /**
     * Read the cache file.
     */
    private synchronized <T> T jsonFromFile(String fileName, T def, Type clasz) {
        T ret = def;

        File f = new File(CACHE_BASE_PATH + "/" + fileName + CACHE_FILE_SUFFIX);
        try {
            if (f.exists()) {
                logger.debug("Cache file: {} exist.", f.getName());
                JsonReader jReader = new JsonReader(new FileReader(f));
                ret = gson.fromJson(jReader, clasz);
                jReader.close();
            } else {
                logger.debug("Cache file: {} not exist.", f.getName());
                if (def != null) {
                    logger.debug("Cache file: {} not exist. Default passed, creating it...", f.getName());
                    jsonToFile(fileName, def, clasz);
                } else {
                    logger.warn("Cache file: {} not exist. Default NOT passed, cache won't be created!", f.getName());
                }
            }
        } catch (Exception e) {
            logger.error("Cache reading throws an exception, cause: {} ({})", e.getClass(), e.getMessage());
        }

        logger.debug("Cache ({}) content: {}", f.getName(), ret);
        return ret;
    }

    /**
     * Write the cache file.
     */
    private synchronized <T> void jsonToFile(String fileName, T obj, Type clasz) {
        JsonWriter jsonWriter = null;
        try {
            File f = new File(CACHE_BASE_PATH + "/" + fileName + CACHE_FILE_SUFFIX);

            jsonWriter = new JsonWriter(new FileWriter(f));

            logger.debug("Writing on cache {}, content: {}", f.getName(), gson.toJson(obj, clasz));
            gson.toJson(obj, clasz, jsonWriter);
        } catch (Exception e) {
            logger.error("Cache writing throws an exception, cause: {} ({})", e.getClass(), e.getMessage());
        } finally {
            if (jsonWriter != null) {
                try {
                    jsonWriter.close();
                } catch (IOException e) {
                    logger.error("Cannot close Json writer");
                }
            }
        }
    }

    public void deleteCache(String fileName) {
        File f = new File(CACHE_BASE_PATH + "/" + fileName + CACHE_FILE_SUFFIX);
        if (f.exists()) {
            if (f.delete()) {
                logger.debug("Cache {} file deleted", f.getName());
            } else {
                logger.error("Cannot delete cache {}", f.getName());
            }
        } else {
            logger.warn("Cache {} not exist", f.getName());
        }
    }
}
