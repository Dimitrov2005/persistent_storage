package com.impl;

import com.api.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

public class PersistentStorage implements Storage {
    private Map<String, Object> persistentStorageMap;
    private Logger logger;
    private Persister persister;

    /**
     * Implementation of persistent storage
     * @param storageFileName - name of the file on which
     *                        will be used as local storage
     */
    public PersistentStorage(String storageFileName) {
        this.logger = LogManager.getLogger(PersistentStorage.class);
        this.persister = new Persister(storageFileName);
        this.persistentStorageMap = persister.getEntriesFromLocalStorage();
    }

    public void put(String key, Object value) {
        //Persist the value(write to local file)
        persister.put(key,(Serializable)value);

        //Update the map
        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.replace(key, value);
            logger.info(String.format("Update entry value for key=%s, value=%s", key, value));
        } else {
            persistentStorageMap.put(key, value);
            logger.info(String.format("Add new entry to database with key=%s, value=%s", key, value));
        }
    }

    public Object get(String key) {
        return persistentStorageMap.get(key);
    }

    public boolean contains(String key) {
        return persistentStorageMap.containsKey(key);
    }

    public boolean remove(String key) {
        //Remove from the persistent DB
        persister.remove(key);

        //Remove from map
        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.remove(key);
            return true;
        }else {
            logger.error("Could not remove the key from the local map");
            return false;
        }
    }
}
