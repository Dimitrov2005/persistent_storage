package com.impl;

import com.api.PersisterBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PersisterJavaSerialization extends PersisterBase{

    private Logger logger;
    protected String storageFileName;
    protected File psFile;
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
    public Map<String, Object> getMapFromLocalFile() {
        Map<String,Object> deserializedMap = new HashMap<>();

        if(psFile.exists() && psFile.canRead()) {
            try ( FileInputStream file = new FileInputStream(psFile);
                  ObjectInputStream in = new ObjectInputStream(file)){

                deserializedMap = (Map<String, Object>) in.readObject();
                logger.info("Retrieved local map from file " + storageFileName);

            } catch (EOFException e) {
                logger.info("", e);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("", e);
                throw new RuntimeException();
            }
        }
        logger.info("New hash map created for persistence");
        return deserializedMap;
    }

    public <K,V> void persistMap(Map<K,V> mapToPersist) {
        try(FileOutputStream file = new FileOutputStream(psFile);
            ObjectOutputStream out = new ObjectOutputStream(file) ){

            //Clean old database
            file.flush();
            //Write new map
            out.writeObject(mapToPersist);

            logger.info("Serialize map object and write to file");
        } catch(IOException e) {
            logger.error("",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void EraseLocalStorage() {
        try (FileOutputStream file = new FileOutputStream(psFile)) {
            file.flush();
        } catch (IOException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

}
