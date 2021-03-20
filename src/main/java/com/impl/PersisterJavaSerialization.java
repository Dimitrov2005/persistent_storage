package com.impl;

import com.api.PersisterBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PersisterJavaSerialization extends PersisterBase{

    private String storageFileName;
    private Logger logger;
    private File psFile;

    public PersisterJavaSerialization(String fileName){
        this.logger = LogManager.getLogger(PersisterJavaSerialization.class);
        this.storageFileName = fileName;
        this.psFile = new File(fileName);
    }

    @Override
    public Map<String, Object> getEntriesFromLocalStorage() {
        Map<String,Object> deserializedMap = new HashMap<>();
        if(psFile.exists() && psFile.canRead()) {
            try {
                FileInputStream file = new FileInputStream(psFile);
                ObjectInputStream in = new ObjectInputStream(file);

                deserializedMap = (Map<String, Object>) in.readObject();
                logger.info("Retrieved local map from file " + storageFileName);

                in.close();
                file.close();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("", e);
                throw new RuntimeException(e);
            }
        }
        return deserializedMap;
    }

    public <K,V> void persistMap(Map<K,V> mapToPersist) {
        try {
            FileOutputStream file = new FileOutputStream(psFile);
            ObjectOutputStream out = new ObjectOutputStream(file);

            //Clean old database
            file.flush();
            //Write new map
            out.writeObject(mapToPersist);

            out.close();
            file.close();
            logger.info("Serialize map object and write to file");

        } catch(IOException e) {
            logger.error("",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void EraseLocalStorage() {
        try {
            FileOutputStream file = new FileOutputStream(psFile);
            file.flush();
            file.close();
        } catch (IOException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

}
