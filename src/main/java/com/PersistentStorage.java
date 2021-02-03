package main.java.com;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class PersistentStorage {

    private HashMap<String, Object> persistentStorageMap;
    private File psFile = new File("persistentStorageBase.txt");
    private final Logger logger = LogManager.getLogger(Main.class);

    PersistentStorage() {
        persistentStorageMap = new HashMap<String, Object>();
        populateStorageMap(persistentStorageMap);
        try {
            if (psFile.createNewFile()) {
            logger.info("New database file for persistent storage created: \n" + psFile.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void put(String key, Object value)  {
        try{
            //Read file for already existing key, if found - update
            Scanner psFileScan = new Scanner(psFile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String psRawText = "";
            boolean keyExists = false;
            while(psFileScan.hasNextLine()){
                String data = psFileScan.nextLine();
                String [] tmp = data.split(" ");
                if (tmp[0].equals(key)) {
                    logger.info(String.format("The key is already inside the db : %s \n ", key));
                    oldK = tmp[0];
                    oldV = tmp[1];
                    keyExists = true;
                }
                buffer.append(data + System.lineSeparator());
            }

            //If the key exists, update the value, else add new entry
            if(keyExists) {
                psRawText = buffer.toString();
                psRawText = psRawText.replaceAll(String.format("%s %s",oldK,oldV), String.format("%s %d",key,value));
            } else {
                buffer.append(String.format("%s %d", key,value) + System.lineSeparator());
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
                persistentStorageMap.replace(key, value);
                logger.info(String.format("Update entry value for key=%s, value=%d \n",key,value));
            }
            else {
                persistentStorageMap.put(key, value);
                logger.info(String.format("Add new entry to database with key=%s, value=%d \n", key, value));
            }
            //TODO: Make smarter - via cast check for object type and write based on it in DB

        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }

    Object get(String key) {
        Scanner psFileScan;
        Object obj = new Object();
        try {
           //load the map from the file
           psFileScan = new Scanner(psFile);
           while (psFileScan.hasNextLine()) { //Improve: parse based on data red
               String data = psFileScan.nextLine();
               String[] kv = data.split(" ");
               if (kv[0].equals(key)) {
                   obj = kv[1];
               }
           }
           psFileScan.close();
        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
        return obj;
    }

    boolean contains(String key) {
        return persistentStorageMap.containsKey(key) ? true : false;
    }

    boolean remove(String key) {
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
                    logger.info(String.format("The key to be removed is inside the db : %s \n ", key));
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

    void populateStorageMap(HashMap<String,Object> map){
        Scanner psFileScan;
        try {
            //load the map from the file
            psFileScan = new Scanner(psFile);
            logger.info("Loading file persistentStorageBase.txt");
            while (psFileScan.hasNextLine()) { //Improve: parse based on data red
                String data = psFileScan.nextLine();
                String[] kv = data.split(" ");
                if(kv.length >= 2) {
                    String k = kv[0];
                    String v = kv[1];
                    persistentStorageMap.put(k, v);
                } else logger.info("Error - String split did not get any values");
            }
            psFileScan.close();

        } catch (IOException e) {
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }
}

