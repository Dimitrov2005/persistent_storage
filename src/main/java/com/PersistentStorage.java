package com;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PersistentStorage {

    private static HashMap<String, Object> persistentStorageMap;
    private static File PSfile = new File("persistentStorageBase.txt");

    PersistentStorage() {
        persistentStorageMap = new HashMap<String, Object>();
        populateStorageMap(persistentStorageMap);
        try {
            if (PSfile.createNewFile()) {
            System.out.println("New database file for persistent storage created: \n" + PSfile.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void put(String key, Object value)  {
        try{
            //Read file for already existing key, if found - update
            Scanner PSfileScan = new Scanner(PSfile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String PSrawText = "";
            boolean keyExists = false;
            while(PSfileScan.hasNextLine()){
                String data = PSfileScan.nextLine();
                String [] tmp = data.split(" ");
                if (tmp[0].equals(key)) {
                    System.out.format("The key is already inside the db : %s \n ", key);
                    oldK = tmp[0];
                    oldV = tmp[1];
                    keyExists = true;
                }
                buffer.append(data + System.lineSeparator());
            }

            //If the key exists, update the value, else add new entry
            if(keyExists) {
                PSrawText = buffer.toString();
                PSrawText = PSrawText.replaceAll(String.format("%s %s",oldK,oldV), String.format("%s %d",key,value));
            } else {
                buffer.append(String.format("%s %d", key,value) + System.lineSeparator());
                PSrawText = buffer.toString();
            }

            PSfileScan.close();

            //Write the new buffer to the file:
            FileWriter PSWriter = new FileWriter("persistentStorageBase.txt");
            PSWriter.flush();
            PSWriter.write(PSrawText);
            PSWriter.close();

            //Now handle the local map
            if(persistentStorageMap.containsKey(key)) {
                persistentStorageMap.replace(key, value);
                System.out.format("Update entry value for key=%s, value=%d \n",key,value);
            }
            else {
                persistentStorageMap.put(key, value);
                System.out.format("Add new entry to database with key=%s, value=%d \n", key, value);
            }
            //TODO: Make smarter - via cast check for object type and write based on it in DB

        } catch (IOException e) {
            System.out.println("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }

    Object get(String key) {
        Scanner PSfileScan;
        Object obj = new Object();
        try {
           //load the map from the file
           PSfileScan = new Scanner(PSfile);
           while (PSfileScan.hasNextLine()) { //Improve: parse based on data red
               String data = PSfileScan.nextLine();
               String[] kv = data.split(" ");
               if (kv[0].equals(key)) {
                   obj = kv[1];
               }
           }
           PSfileScan.close();
        } catch (IOException e) {
            System.out.println("An error occurred when trying to create or load PSDB.");
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
            Scanner PSfileScan = new Scanner(PSfile);
            StringBuffer buffer = new StringBuffer();
            String oldK = "";
            String oldV = "";
            String PSrawText = "";

            //Read file for already existing key, if found - remove else - return false
            while (PSfileScan.hasNextLine()) {
                String data = PSfileScan.nextLine();
                String[] tmp = data.split(" ");
                if (tmp[0].equals(key)) {
                    System.out.format("The key to be removed is inside the db : %s \n ", key);
                    oldK = tmp[0];
                    oldV = tmp[1];
                    keyExists = true;
                }
                buffer.append(data + System.lineSeparator());
            }

            //If the key exists, remove the value, remove the blank line, else do nothing
            if (keyExists) {
                PSrawText = buffer.toString();
                PSrawText = PSrawText.replaceAll(String.format("%s %s", oldK, oldV), "");
                PSrawText = PSrawText.replaceAll("[\\\r\\\n]+", "");
                rmStorage = true;
            }
            PSfileScan.close();

            //Write the new buffer to the file:
            FileWriter PSWriter = new FileWriter("persistentStorageBase.txt");
            PSWriter.flush();
            PSWriter.write(PSrawText);
            PSWriter.close();
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
        Scanner PSfileScan;
        try {
            //load the map from the file
            PSfileScan = new Scanner(PSfile);
            System.out.println("Loading file persistentStorageBase.txt");
            while (PSfileScan.hasNextLine()) { //Improve: parse based on data red
                String data = PSfileScan.nextLine();
                String[] kv = data.split(" ");
                if(kv.length >= 2) {
                    String k = kv[0];
                    String v = kv[1];
                    persistentStorageMap.put(k, v);
                } else System.out.println("Error - String split did not get any values");
            }
            PSfileScan.close();

        } catch (IOException e) {
            System.out.println("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }
}

