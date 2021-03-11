package com.api;

import java.io.Serializable;
import java.util.Map;

public abstract class PersisterBase {
    /**
     * Get all entries from the local DB and put them
     * into a map object.
     * @return a full map of entries from the local DB
     */
    public abstract Map<String,Object> getEntriesFromLocalStorage();

    /**
     * Erase all contents in the local file
     */
    protected abstract void EraseLocalStorage();

    /**
     * Put a new entry in the local file consisting of:
     * @param key - key assoc with value
     * @param value - value for the key
     */
   /// public abstract void put(String key, Object value);
;
    /**
     * Get the object associated with the key
     * @param key
     * @return Object
     */
    //public abstract Object get(String key)

    /**
     * Check if element is contained in local file
     */
    //public abstract boolean contains(String key);

    /**
     * Remove the object from the local file associated with key
     * @param Key
     */
   // public abstract boolean remove(String Key);

    /**
     * Decoding of the class based on a String.
     * @param name - class name
     * @return Class of type Class
     */
    //public abstract Class decodeClass(String name);
}
