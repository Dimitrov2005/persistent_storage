package com.impl;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        Map<String, Object> testMapWrite = new HashMap<>();
        Map<String, Object> testMapRead = new HashMap<>();
        testMapWrite.put("try1", 10);
        testMapWrite.put("try2",15);
        testMapWrite.put("try3",new Object());

        Map<String,Serializable> testMapSer  = testMapWrite.entrySet().stream()
                .filter(e -> e.getValue() instanceof Serializable)
                .collect(Collectors.toMap(Map.Entry::getKey, e->(Serializable) e.getValue()));
        Set<Map.Entry<String,Object>> setWriteMap = testMapWrite.entrySet();
        Set<Map.Entry<String,Serializable>> setSerMap = testMapSer.entrySet();
        Set<Map.Entry<String,Object>> diff = new HashSet<>(setWriteMap);
        diff.removeAll(setSerMap);

        System.out.println("The following items will not be serialized hence will be lost :" + diff);

        // Serialization
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream("testFile.txt");
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(testMapSer);

            out.close();
            file.close();

            System.out.println("MAP has been serialized");

        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("IOException is caught");
        }


        // Deserialization
        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream("testFile.txt");
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            testMapRead = (Map<String, Object>) in.readObject();

            in.close();
            file.close();

            System.out.println("Map has been deserialized ");
            System.out.println(testMapRead.entrySet());

        } catch(IOException ex) {
            System.out.println("IOException is caught");
        } catch(ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        }
    }
}
