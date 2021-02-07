package com.ps;

public abstract class Main {

    private static PersistentStorageNoStd persistentStorage;

    public static void main(String[] args) {
       /* Test Persistent Storage with std libs
        persistentStorage = new PersistentStorage();
        persistentStorage.put("anotherString", 12368);
        persistentStorage.put("anotherString12", 16);

        System.out.format("got value from db = %s \n ", persistentStorage.get("anotherString12"));

        persistentStorage.remove("anotherString");
        */

        /*  Test Persistent Storage without std libs
         */
        persistentStorage = new PersistentStorageNoStd(40, 100);

        persistentStorage.put("anotherString", 12368);
        persistentStorage.put("anotherString12", 16);
        System.out.format("got value from db = %s \n ", persistentStorage.get("anotherString12"));
        persistentStorage.remove("anotherString");



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