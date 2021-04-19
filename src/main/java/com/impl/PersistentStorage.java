package com.impl;

import com.api.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class PersistentStorage implements Storage {
    private Map<String, Object> persistentStorageMap;
    private Logger logger;
    private PersisterJavaSerialization persister;

    /**
     * Implementation of persistent storage
     * @param storageFileName - name of the file on which
     *                        will be used as local storage
     */
    public PersistentStorage(String storageFileName) {
        this.logger = LogManager.getLogger(PersistentStorage.class);
        this.persister = new PersisterJavaSerialization(storageFileName);
        this.persistentStorageMap = persister.getEntriesFromLocalStorage();
    }

    public void put(String key, Object value) {
        //Update the map
        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.replace(key, value);
            logger.info(String.format("Update entry value for key=%s, value=%s", key, value));
        } else {
            persistentStorageMap.put(key, value);
            logger.info(String.format("Add new entry to database with key=%s, value=%s", key, value));
        }
        //Persist the value(write to local file)
        persister.persistMap(persistentStorageMap);
    }

    public Object get(String key) {
        return persistentStorageMap.get(key);
    }

    public boolean contains(String key) {
        return persistentStorageMap.containsKey(key);
    }

    public boolean remove(String key) {

        //Remove from map
        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.remove(key);

            persister.persistMap(persistentStorageMap);
            return true;
        }else {
            logger.error("Could not remove the key from the local map");
            return false;
        }

    }
}
