package com;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        final Logger logger = LogManager.getLogger(Main.class);

        /* Test Persistent Storage with std libs
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //Configure JSON to detect all fields (public, private,etc)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Integer intVal = 10;
        String info = mapper.writeValueAsString(tmp);
        logger.info(info);
        */

        Temp tmp  = new Temp();
        PersistentStorage persistentStorage = new PersistentStorage("text.txt");

        persistentStorage.put("anotherString", 12368);
        persistentStorage.put("anotherString12",tmp );
        Object obj = persistentStorage.get("anotherString12");

        //System.out.format("got value from db = %s \n ", persistentStorage.get("anotherString12"));
        //persistentStorage.remove("anotherString");


        //logger.info("The class in json:" + gson.toJson(persistentStorage));

        /*  Test Persistent Storage without std libs

        PersistentStorage persistentStorage;
        persistentStorage = new PersistentStorage("sss.txt");
        persistentStorage.put("anotherString", 12368);
        persistentStorage.put("anotherString12", 16);
        logger.info(String.format("got value from db = %s \n ", persistentStorage.get("anotherString12")));
        persistentStorage.remove("anotherString");
        */


        /* Test Map Structure
        MapStruct ms = new MapStruct();
        char [] key = new char [20];
        key[0] = 'a';
        key[1] = 's';
        char [] value = new char [20];
        value[0] = '1';
        value[1] = '2';
        char [] key1 = new char [20];
        key1[0] = 'b';
        key1[1] = 's';
        char [] value1 = new char [20];
        value1[0] = '2';
        value1[1] = '2';
        ms.put(key, value);
        ms.put(key1, value1);
        char[] keyRed = ms.getValue(1);
       System.out.println(String.format("Got 1'st element of the storage key = %s \n", String.valueOf(keyRed)));
       char [] valByKey = ms.getValueByKey(key);
       System.out.format("Got the value %s \n", String.valueOf(valByKey));
       ms.contains(key1);
       ms.remove(key1);
 */

    }
}
