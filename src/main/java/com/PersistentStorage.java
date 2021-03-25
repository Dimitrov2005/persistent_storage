package com;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class PersistentStorage implements Storage {
    private Map<String, Object> persistentStorageMap;
    private String storageFileName;
    private File psFile;
    private Logger logger;
    private ObjectMapper mapper;

    /**
     * Implementation of persistent storage
     * @param storageFileName - name of the file on which the
     *                        will be used as local storage
     */
    public PersistentStorage(String storageFileName) {
        this.storageFileName = storageFileName;
        this.psFile = new File(storageFileName);
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

    public void put(String key, Object value) {
        //Put node into local storage
        boolean putInLocalStorage = false;
        String valueToJson = null;
        try {
            valueToJson = mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        putInLocalStorage = updateLocalStorageNode(key, valueToJson);
        if (putInLocalStorage == false) logger.error("Node not stored into local storage. Key=" + key);

        //Update the local map
        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.replace(key, valueToJson);
            logger.info(String.format("Update entry value for key=%s, value=%s", key, valueToJson));
        } else {
            persistentStorageMap.put(key, valueToJson);
            logger.info(String.format("Add new entry to database with key=%s, value=%s", key, valueToJson));
        }
    }

    public Object get(String key) {
        Object objLocalStorage = new Object();
        objLocalStorage = getObjectFromLocalStorage(key);
        return objLocalStorage;
    }

    public boolean contains(String key) {
        boolean containsLocalStorage = containedInLocalStorage(key);
        boolean containsLocalMap = persistentStorageMap.containsKey(key);
        return containsLocalMap && containsLocalStorage ;
    }

    public boolean remove(String key) {
        boolean rmLocalMap = false;
        boolean rmLocalStorage = removeLocalStorageNode(key);

        if (persistentStorageMap.containsKey(key)) {
            persistentStorageMap.remove(key);
            rmLocalMap = true;
        }
        return rmLocalStorage && rmLocalMap;
    }

    //Private methods for manipulation of the local storage(text file)

    /**
     * Load values from local storage file into the hash map.
     * @param map - Map to load the values into
     */
    private void populateStorageMap(Map<String, Object> map) {
        try {
            //load the map from the file
            Scanner psFileScan;
            psFileScan = new Scanner(psFile);
            logger.info("Loading file: " + storageFileName);
            while (psFileScan.hasNextLine()) {
                String node = psFileScan.nextLine();
                String[] kv = node.split(" ");
                if (kv.length >= 2) {
                    logger.info("put value into local map" + node.replaceFirst(kv[0],""));
                    persistentStorageMap.put(kv[0], mapper.writeValueAsString(node.replaceFirst(kv[0],"")));
                } else logger.info("String split did not get any values, maybe storage file is empty");
            }
            psFileScan.close();
        } catch (IOException e) {
            logger.error("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
    }

    /**
     * Get Object from local storage associated with the key
     * @param key key associated with the Object
     * @return the Object
     */
    private Object getObjectFromLocalStorage(String key) {
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
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Check if key is contained in the local storage
     * @param key - Key to be checked
     * @return true if key is found or false if not
     */
    private boolean containedInLocalStorage(String key) {
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
            e.printStackTrace();
        }
        return false;
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
            logger.info("An error occurred when trying to create or load PSDB.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove node from the local storage
     * @param key - key associated with the node
     * @return - true if the node is removed from the storage
     */
    public boolean removeLocalStorageNode(String key) {
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
            e.printStackTrace();
        }
        return rmStatus;
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
            logger.error("Write to file failed" + fileName);
            e.printStackTrace();
        }
    }
}

