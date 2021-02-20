package com.impl;
import com.api.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


//DONE 1.A class that does all the required read(on creation of the PS) /write(when new entry is there)
// delete (when entry.remove) to make the class persistent. (a persister basically)
//DONE 2. All e.print to be logged with logger

//TODO 3. Unit test for every operation (Put, Read, Remove, Contains)
/* TODO 4. Unit test to check i.e whether DB is persistent (create one instance, write entries, create other, read/
    should be the same
*/
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
        persister.put(key,value);

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
