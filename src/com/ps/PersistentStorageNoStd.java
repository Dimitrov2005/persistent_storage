package com.ps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class PersistentStorageNoStd {

    private static MapStruct persistentStorageMap;
    private static File PSfile = new File("persistentStorageBase.txt");
    private int lengthOfEntry;

    PersistentStorageNoStd(int lengthOfEntry, int storageSize) {
        this.lengthOfEntry = lengthOfEntry;
        persistentStorageMap = new MapStruct(lengthOfEntry, 100);
        populateStorageMap(persistentStorageMap);
        try {
            if (PSfile.createNewFile()) {
                System.out.println("New database file for persistent storage created: \n" + PSfile.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    char [] toCharArray (String str){
        char [] array = new char[lengthOfEntry/2];
        for (int i = 0; i<str.length(); i++){
            array[i] = str.charAt(i);
        }
        return array;
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
            if(persistentStorageMap.contains(toCharArray(key))) {
                persistentStorageMap.replace(toCharArray(key), toCharArray(value.toString()));
                System.out.format("Update entry value for key=%s, value=%d \n",key,value);
            }
            else {
                persistentStorageMap.put(toCharArray(key), toCharArray(value.toString()));
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
        return persistentStorageMap.contains(toCharArray(key));
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

        if (persistentStorageMap.contains(toCharArray(key))) {
            persistentStorageMap.remove(toCharArray(key));
            rmLocal = true;
        }

        return rmLocal && rmStorage;
    }

    void populateStorageMap(MapStruct map){
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
                    persistentStorageMap.put(toCharArray(k), toCharArray(v));
                } else System.out.println("Error - String split did not get any values");
            }
            PSfileScan.close();
        } catch (IOException e) {
            System.out.println("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }

}
