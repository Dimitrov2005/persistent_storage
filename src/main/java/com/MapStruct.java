package com;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;

public class MapStruct {

    private int keyLength = 20;   //Default length of the key
    private int valueLength = 20; //Default length of the value
    private int storageSize = 10; //How many entries
    private char [] key;
    private char [] value;
    private char [] entry;
    private char [][] storage;
    private int storageWritePointer;

    private final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Class MapStruct - when creating an instance pass
     * lengthOfEntry - length of the char array for the
     *      whole entry <key, value>
     * storageSize - How many entries will the storage
     *      contain.
     * Note that if the storage is full, then
     * it will overfill and overwrite from the first
     * element.
     */
    MapStruct(int lengthOfEntry, int storageSize){
        this.keyLength = lengthOfEntry / 2;
        this.valueLength = lengthOfEntry / 2;
        this.storageSize = storageSize;
        this.key = new char[keyLength];
        this.value = new char[valueLength];
        this.entry = new char[keyLength + valueLength];
        this.storage = new char[storageSize][keyLength+valueLength];
        this.storageWritePointer = 0;
    }

    /**
     * Returns the key of the entity at a given position
     * in the storage. Returned result is a char array
     */
    char[] getKey(int position) {
        char[] key = new char[keyLength];
        for(int i =0; i<keyLength; i++){
            key[i] = storage[position][i];
        }
        return key;
    }

    /**
     * Returns the value of the entity at a given position
     * in the storage. Returned result is a char array
     */
    char[] getValue(int position) {
        char[] val = new char[valueLength];
        for(int i = keyLength; i<keyLength+valueLength; i++){
            val[i-keyLength] = storage[position][i];
        }
        return val;
    }

    /**
     * Returns the value corresponding to the key.
     * if the key does not exist then will return an
     * empty char array.
    */
    char[] getValueByKey(char[] key) {
        char[] curKey = new char[keyLength];
        char[] val = new char[valueLength];
        for(int j = 0 ; j<storageSize; j++){
            for(int i = 0; i<keyLength; i++){
                curKey[i] = storage[j][i];
            }
            if(Arrays.equals(curKey,key)){
                for(int i = keyLength; i<keyLength+valueLength; i++){
                    val[i-keyLength] = storage[j][i];
                }
                return val;
            }
        }
        return val;
    }

    /**
     * Inserts new entity{key,value} in the storage.
     * If the storage is full, then it will overfill
     * and overwrite from the first element.
     */
    void put (char[] key, char[] value){
        for (int i = 0; i<keyLength; i++){
            storage [storageWritePointer][i] = key[i];
        }
        for (int i = keyLength; i<keyLength+valueLength; i++){
            storage [storageWritePointer][i] = value[i-20];
        }
        if(storageWritePointer == storageSize)
            storageWritePointer = 0;
        else storageWritePointer++;
    }

    /**
    * Function returns true if
    * the char array key is present
    * in the current storage
    * if not - returns false
    */
    boolean contains(char[] key){
        char[] curKey = new char[keyLength];
        char[] val = new char[valueLength];
        for(int j = 0 ; j<storageSize; j++){
            for(int i = 0; i<keyLength; i++){
                curKey[i] = storage[j][i];
            }
            if(Arrays.equals(curKey,key)){
                return true;
            }
        }
        return false;
    }

    /**
     * Function returns true if
     * the char array key is present
     * and has been removed successfully
     * from the current storage
     * if not - returns false
     */
    boolean remove(char[] key){
        char[] curKey = new char[keyLength];
        char[] val = new char[valueLength];
        for(int j = 0 ; j<storageSize; j++){
            for(int i = 0; i<keyLength; i++){
                curKey[i] = storage[j][i];
            }
            if(Arrays.equals(curKey,key)){
                for(int i = 0; i<keyLength+valueLength; i++){
                    storage[j][i] = ' ';
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Function returns true if
     * the char array key is present
     * and has been replaced successfully
     * with the given key and value args
     * if not - returns false
     */
    boolean replace(char[] key, char[] value){
        char[] curKey = new char[keyLength];
        char[] curVal = new char[valueLength];
        for(int j = 0 ; j<storageSize; j++){
            for(int i = 0; i<keyLength; i++){
                curKey[i] = storage[j][i];
            }
            if(Arrays.equals(curKey,key)){
                for(int i = 0; i<keyLength; i++){
                    storage[j][i] = key[i];
                }
                for (int i = keyLength; i<keyLength+valueLength; i++){
                    storage [j][i] = value[i-20];
                }
                return true;
            }
        }
        return false;
    }

}
