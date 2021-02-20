package com.impl;

import com.api.PersisterBase;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Persister extends PersisterBase {

    //All vars private
    //Methods public/private/protected
    private String storageFileName;
    private File psFile;
    private Logger logger;
    private ObjectMapper mapper;

    public Persister(String fileName){
        this.logger = LogManager.getLogger(Persister.class);
        this.storageFileName = storageFileName;
        this.psFile = new File(storageFileName);
        this.mapper = new ObjectMapper();

        //Disable exception on empty bean
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //Configure JSON to detect all fields (public, private,packPrivate, protected)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Configure JSON to have the class name as first string of serialization
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        //Create the file of the persistent storage which is used by read/write/ functionalities
        try {
            if (psFile.createNewFile()) {
                logger.info("New database file for persistent storage created:" + psFile.getName());
            }
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    @Override
    public Map<String,Object> getEntriesFromLocalStorage() {
        Map<String, Object> tempMap = new HashMap<String, Object>();
        try {
            //load the map from the file
            Scanner psFileScan;
            psFileScan = new Scanner(psFile);
            logger.info("Loading file: " + storageFileName);
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] kv = node.split(" ");
                if (kv.length >= 2) {
                    logger.info("put value into temporary map" + node.replaceFirst(kv[0], ""));
                    tempMap.put(kv[0], mapper.writeValueAsString(node.replaceFirst(kv[0], "")));
                } else logger.info("String split did not get any values, maybe storage file is empty");
            }
            psFileScan.close();
        } catch (IOException e) {
            logger.error("An error occurred when trying to create or load PSDB.", e);
        }
        return tempMap;
    }

    @Override
    public void put(String key, Object value) {
        String valueToJson = null;
        boolean putInLocalStorage = false;

        try {
            valueToJson = mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("",e);
        }
        putInLocalStorage = updateLocalStorageNode(key, valueToJson);
        if (putInLocalStorage == false) {
            logger.error("Node not stored into local storage. Key=" + key);
            throw new RuntimeException();
        }
    }

    @Override
    public Object get(String key) {
        Object obj = new Object();
        try {
            Scanner psFileScan;
            psFileScan = new Scanner(psFile);
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] kv = node.split(" ");
                if (kv.length >= 2 && kv[0].equals(key)) {
                    obj = mapper.readValue(node.replaceFirst(kv[0],""), Object.class);
                    logger.info("Retrieved value from local storage:" + node.replaceFirst(kv[0],""));
                    return obj;
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred when trying to create or load PSDB",e);
            throw new RuntimeException(e);
        }
        return obj;
    }

    @Override
    public boolean contains(String key) {
        try {
            Scanner psFileScan;
            psFileScan = new Scanner(psFile);
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] kv = node.split(" ");
                if (kv.length >= 2 && kv[0].equals(key)) {
                    return true;
                }
            }
        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean remove(String key) {
        boolean rmStatus = false;
        try {
            Scanner psFileScan = new Scanner(psFile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String psRawText = "";
            boolean keyExists = false;

            //Read file for already existing key, if found - remove else - return false
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] keyValueSeparated = node.split(" ");
                if (keyValueSeparated[0].equals(key)) {
                    logger.info(String.format("The key to be removed is inside the db : %s ", key));
                    oldK = keyValueSeparated[0];
                    oldV = node.replaceFirst(keyValueSeparated[0],"");
                    keyExists = true;
                }
                buffer.append(node + System.lineSeparator());
            }

            //If the key exists, remove the value, remove the blank line, else do nothing
            if (keyExists) {
                psRawText = buffer.toString();
                psRawText = psRawText.replace(String.format("%s %s", oldK, oldV), "");
                psRawText = psRawText.replace("[\\\r\\\n]+", "");
                rmStatus = true;
            }
            psFileScan.close();
            //Write the new buffer to the file:
            writeToFile(storageFileName,psRawText);
        } catch (IOException e) {
            logger.error("Could not remove the key from persistent DB",e);
            throw new RuntimeException();
        }
        return rmStatus;
    }

    @Override
    protected void EraseLocalStorage() {
        writeToFile(storageFileName, "");
    }

    /**
     * Comparison between other objects and Persister;
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        //Self Check
        if(this == obj)
            return true;
        //Null Check
        if(obj == null)
            return false;
        //Type Check
        if(getClass() != obj.getClass())
            return false;

        /* Fields Check :
            Get all key - value from current object and
            compare with all key - value pairs. If there
            is one difference - return false
        */
        Persister persister = (Persister) obj;
        List<String> keys = this.getKeys();
        for (String key : keys) {
            if(persister.contains(key)){
                Object valueToCompare = persister.get(key);
                if(! valueToCompare.equals(this.get(key)))
                    return false;
            }else
                return false;
        }
        return true;
    }

    /**
     * Put a new node into the local storage
     * @param key Key associated with the value
     * @param value the Object as a string
     * @return true if new node is created
     */
    private boolean updateLocalStorageNode(String key, String value) {
        try {
            Scanner psFileScan = new Scanner(psFile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String psRawText;
            boolean keyExists = false;

            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] keyValueSeparated = node.split(" ");
                if (keyValueSeparated[0].equals(key)) {
                    logger.info(String.format("The key is already inside the db : %s", key));
                    oldK = keyValueSeparated[0];
                    oldV = node.replaceFirst(keyValueSeparated[0],"");
                    keyExists = true;
                }
                buffer.append(node + System.lineSeparator());
            }

            //If the key exists, update the value, else add new entry
            if (keyExists) {
                psRawText = buffer.toString();
                psRawText = psRawText.replace(String.format("%s %s", oldK, oldV), String.format("%s %s", key, value));
            } else {
                buffer.append(String.format("%s %s", key, value) + System.lineSeparator());
                psRawText = buffer.toString();
            }
            psFileScan.close();
            //Write the new buffer to the file:
            writeToFile(storageFileName,psRawText);
            return true;
        } catch (IOException e) {
            logger.error("",e);
        }
        return false;
    }

    /**
     * Write a String to a file
     * @param fileName - the name of the file
     * @param text - String to be written
     */
    private void writeToFile(String fileName, String text){
        try{
            FileWriter psWriter = new FileWriter(fileName);
            psWriter.flush();
            psWriter.write(text);
            psWriter.close();
        }catch(IOException e) {
            logger.error("Write to file failed" + fileName,e);
        }
    }

    protected List<String> getKeys(){
        List<String> keys = new ArrayList<String>();
        try {
            Scanner psFileScan = new Scanner(psFile);

            //Read file and put every key into the list
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] keyValueSeparated = node.split(" ");
                keys.add(keyValueSeparated[0]);
            }

            psFileScan.close();

        } catch (IOException e) {
            logger.error("Could not retrieve all keys from persistent DB",e);
            throw new RuntimeException();
        }
        return keys;
    }

}
