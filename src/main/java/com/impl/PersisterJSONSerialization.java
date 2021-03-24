package com.impl;

import com.api.PersisterBase;
import com.api.Storage;
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

public class PersisterJSONSerialization extends PersisterBase implements Storage {

    //All vars private
    //Methods public/private/protected
    private String storageFileName;
    private File psFile;
    private Logger logger;
    private ObjectMapper mapper;

    public PersisterJSONSerialization(String fileName){
        this.logger = LogManager.getLogger(PersisterJSONSerialization.class);
        this.storageFileName = fileName;
        this.psFile = new File(fileName);
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
            throw new RuntimeException();
        }
    }

    @Override
    public Map<String,Object> getEntriesFromLocalStorage() {
        Map<String, Object> tempMap = new HashMap<>();
        try {
            //load the map from the file
            Scanner psFileScan;
            psFileScan = new Scanner(psFile);
            logger.info("Loading file: " + storageFileName);
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] kv = node.split(" ");
                if (kv.length >= 2) {
                    //tempMap.put(kv[0], mapper.writeValueAsString(node.replaceFirst(kv[0], "")));
                    String value = node.replaceFirst(kv[0], "");

                    //Split class name and variables
                    String [] splitValue = value.split(":");
                    //Get clean class name
                    String valClassName = splitValue[0].replace("{", "").replace("\"","").trim();
                    //Get clean variables
                    String classVariables = splitValue[1].replace("}","").replace("\"","").trim();

                    //Decode class and return the correct type
                    Class decodedClass = decodeClass(valClassName);

                    //If the JSON read object is primitive type:
                    if(checkPrimitivity(valClassName)){
                        //Returns new variable based on class name
                        Object obj = decodePrimitiveValue(valClassName, classVariables);
                        tempMap.put(kv[0], obj);
                    } else { //TODO : Decoding a object of class T
                        Object obj = mapper.readValue(value, decodedClass);
                        tempMap.put(kv[0], obj);
                    }

                } else logger.info("String split did not get any values, maybe storage file is empty");
            }
            psFileScan.close();
        } catch (IOException e) {
            logger.error("An error occurred when trying to create or load PSDB.", e);
            throw new RuntimeException();
        }
        return tempMap;
    }

    @Override
    public void put(String key, Object value) {
        String valueToJson = null;
        boolean putInLocalStorage;

        try {
            valueToJson = mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("",e);
        }
        putInLocalStorage = updateLocalStorageNode(key, valueToJson);
        if (!putInLocalStorage) {
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
                psRawText = buffer.toString().replace(String.format("%s%s", oldK, oldV), "").replace("[\\\r\\\n]+", "");
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
     * Comparison between other objects and PersisterJSONSerialization;
     * @param obj - comparing
     * @return true if objects match
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
        PersisterJSONSerialization persisterJSONSerialization = (PersisterJSONSerialization) obj;
        List<String> keys = this.getKeys();
        for (String key : keys) {
            if(persisterJSONSerialization.contains(key)){
                Object valueToCompare = persisterJSONSerialization.get(key);
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

    /**
     * Get all keys from the local file
     * @return List of keys
     */
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

    /**
     * This function is to be used when the get is not returning
     * an object but another type. Can be override in the classes
     * extending the persister class
     * @param name of the type
     * @return class of the type
     */
    public Class decodeClass(String name){
        switch (name) {
            case "String"  : return String.class;
            case "Integer" : return Integer.class;
            case "Boolean" : return Boolean.class;
            case "Character" : return Character.class;
            case "Short" : return Short.class;
            case "Double" : return Double.class;
            case "Float" : return Float.class;
            case "Byte" : return Byte.class;
        }
        return Object.class;
    }

    /**
     * Return the value in the correct type
     * @param className Type of the returned result
     * @param value value to be casted
     * @return the correct value of type
     */
    private Object decodePrimitiveValue(String className, String value){
        switch (className){
            case "Integer": return Integer.parseInt(value);
            case "Boolean": return Boolean.parseBoolean(value);
            case "Character" : return value.charAt(0);
            case "Short" : return Short.parseShort(value);
            case "Double" : return Double.parseDouble(value);
            case "Float" : return Float.parseFloat(value);
            case "Byte" : return Byte.parseByte(value);
            default: return value;
        }
    }

    /**
     * Check if className is primitive or
     * the data type corresponding to it.
     * @param className Type
     * @return true if type is within 7 primitive types + String
     *
     */
    private boolean checkPrimitivity(String className) {
        return className.equals("String") || className.equals("Integer") ||
                className.equals("Boolean") || className.equals("Character") ||
                className.equals("Short") || className.equals("Double") ||
                className.equals("Float") || className.equals("Byte");
    }


}
