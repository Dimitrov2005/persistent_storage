package com.impl;

import com.api.PersisterBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PersisterJavaSerialization extends PersisterBase{

    private String storageFileName;
    private Logger logger;
    private File psFile;
    protected String storagePath;
    protected File storageDir;

    public PersisterJavaSerialization(String fileName){

        //Create storage directory
        this.storagePath = Paths.get("").toAbsolutePath().toString() + "\\.persistentDB\\";
        this.storageDir = new File(storagePath);
        storageDir.mkdir();

        this.logger = LogManager.getLogger(PersisterJavaSerialization.class);
        this.storageFileName = fileName;
        this.psFile = new File(storagePath + "\\" + storageFileName);
    }

    @Override
    public Map<String, Object> getEntriesFromLocalStorage() {
        Map<String,Object> deserializedMap = new HashMap<>();

        if(psFile.exists() && psFile.canRead()) {
            ObjectInputStream in = null;
            FileInputStream file = null;
            try {
                file = new FileInputStream(psFile);
                in = new ObjectInputStream(file);

                deserializedMap = (Map<String, Object>) in.readObject();
                logger.info("Retrieved local map from file " + storageFileName);

            } catch (EOFException e) { //Todo : clean up this, this is thrown when new file is created since it is empty
                logger.info("", e);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("", e);
                throw new RuntimeException();
            } finally {
                try {
                    if (in != null) in.close();
                    if (file != null) file.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
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
