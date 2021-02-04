package com;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class PersistentStorage implements Storage {

    private Map<String, Object> persistentStorageMap;
    private String storageFileName;
    private File psFile ;
    private Logger logger ;
    private ObjectMapper mapper ;

    /**
     * Implementation of persistent storage
     * @param storageFileName - name of the file on which the
     *                        will be used as local storage
     */

    PersistentStorage(String storageFileName) {
        this.storageFileName = storageFileName;
        this.psFile= new File(storageFileName);
        this.logger = LogManager.getLogger(PersistentStorage.class);
        this.persistentStorageMap = new HashMap<>();
        this.mapper = new ObjectMapper();

        //Disable exception on empty bean
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //Configure JSON to detect all fields (public, private,packPrivate, protected)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE); // Class name as first string

        try {
            if (psFile.createNewFile()) {
            logger.info("New database file for persistent storage created:" + psFile.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        populateStorageMap(persistentStorageMap);
    }

    public void put(String key, Object value)  {
        try{
            //Read file for already existing key, if found - update
            Scanner psFileScan = new Scanner(psFile);
            StringBuffer buffer = new StringBuffer();
            String valueToJson = mapper.writeValueAsString(value);
            String oldK = "";
            String oldV = "";
            String psRawText;
            boolean keyExists = false;
            while(psFileScan.hasNextLine()){
                String data = psFileScan.nextLine();
                String [] tmp = data.split(" ");
                if (tmp[0].equals(key)) {
                    logger.info(String.format("The key is already inside the db : %s", key));
                    oldK = tmp[0];
                    oldV = tmp[1];
                    keyExists = true;
                }
                buffer.append(data + System.lineSeparator());
            }

            //If the key exists, update the value, else add new entry
            if(keyExists) {
                psRawText = buffer.toString();
                psRawText = psRawText.replaceAll(String.format("%s %s",oldK,oldV), String.format("%s %s",key,valueToJson));
            } else {
                buffer.append(String.format("%s %s", key,valueToJson) + System.lineSeparator());
                psRawText = buffer.toString();
            }
            psFileScan.close();

            //Write the new buffer to the file:
            FileWriter psWriter = new FileWriter("persistentStorageBase.txt");
            psWriter.flush();
            psWriter.write(psRawText);
            psWriter.close();

            //Now handle the local map
            if(persistentStorageMap.containsKey(key)) {
                persistentStorageMap.replace(key, valueToJson);
                logger.info(String.format("Update entry value for key=%s, value=%s",key,valueToJson));
            }
            else {
                persistentStorageMap.put(key, valueToJson);
                logger.info(String.format("Add new entry to database with key=%s, value=%s", key, valueToJson));
            }
        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }

   public Object get(String key) {
        Scanner psFileScan;
        Object obj = new Object();
        try {
           //load the map from the file
           psFileScan = new Scanner(psFile);
           while (psFileScan.hasNextLine()) { //Improve: parse based on data red
               String data = psFileScan.nextLine();
               String[] kv = data.split(" ");
               if (kv[0].equals(key)) {
                   obj = mapper.readValue(kv[1], Object.class);
               }
           }
           psFileScan.close();
        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
        return obj.getClass();
    }

    public boolean contains(String key) {
        return persistentStorageMap.containsKey(key) ? true : false;
    }

    public boolean remove(String key) {
        boolean rmStorage = false;
        boolean rmLocal = false;
        boolean keyExists = false;
        try {
            Scanner psFileScan = new Scanner(psFile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String psRawText = "";

            //Read file for already existing key, if found - remove else - return false
            while (psFileScan.hasNextLine()) {
                String data = psFileScan.nextLine();
                String[] tmp = data.split(" ");
                if (tmp[0].equals(key)) {
                    logger.info(String.format("The key to be removed is inside the db : %s ", key));
                    oldK = tmp[0];
                    oldV = tmp[1];
                    keyExists = true;
                }
                buffer.append(data + System.lineSeparator());
            }

            //If the key exists, remove the value, remove the blank line, else do nothing
            if (keyExists) {
                psRawText = buffer.toString();
                psRawText = psRawText.replaceAll(String.format("%s %s", oldK, oldV), "");
                psRawText = psRawText.replaceAll("[\\\r\\\n]+", "");
                rmStorage = true;
            }
            psFileScan.close();

            //Write the new buffer to the file:
            FileWriter psWriter = new FileWriter("persistentStorageBase.txt");
            psWriter.flush();
            psWriter.write(psRawText);
            psWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.remove(key);
            rmLocal = true;
        }

        return rmLocal && rmStorage;
    }

    private void populateStorageMap(Map<String,Object> map){
        Scanner psFileScan;
        try {
            //load the map from the file
            psFileScan = new Scanner(psFile);
            logger.info("Loading file: " + storageFileName);
            while (psFileScan.hasNextLine()) {
                String data = psFileScan.nextLine();
                String[] kv = data.split(" ");
                if(kv.length >= 2) {
                    persistentStorageMap.put(kv[0], mapper.writeValueAsString(kv[1]));
                } else logger.info("String split did not get any values, maybe storage file is empty");
            }
            psFileScan.close();

        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }
}

