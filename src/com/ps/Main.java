package com.ps;

public abstract class Main {

    private static PersistentStorage persistentStorage;

    public static void main(String[] args) {
        persistentStorage = new PersistentStorage();
        persistentStorage.put("anotherString", 12368);
        persistentStorage.put("anotherString12", 16);

        System.out.format("got value from db = %s \n ", persistentStorage.get("anotherString12"));

        persistentStorage.remove("anotherString");
    }
}
