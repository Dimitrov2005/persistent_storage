package com.api;

public interface Storage {
    /**
     * Store a value under a given key
     * @param key String representing the key
     * @param value Object representing value
     */
    void put(String key, Object value);

    /**
     * Retrieve the value for the given key
     * @param key String representing the key
     * @return Object associated with the key
     *      or null if the key is not mapped
     */
    Object get(String key);

    /**
     * Check if key already exists in persistent storage
     * @param key String representing the key
     * @return true if key is already used to
     * store a value
     */
    boolean contains(String key);

    /**
     * Remove the key-value pair from the persistent storage
     * @param key String representing the key
     * @return true if the key-value pair is removed
     */
    boolean remove(String key);

}
