package com.api;

import java.io.Serializable;
import java.util.Map;

public abstract class PersisterBase {
    /**
     * Get all entries from the local DB and put them
     * into a map object.
     * @return a full map of entries from the local DB
     */
    public abstract Map<String,Object> getMapFromLocalFile();

    /**
     * Erase all contents in the local file
     */
    protected abstract void EraseLocalStorage();


}
