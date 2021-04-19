import static org.junit.jupiter.api.Assertions.assertEquals;

import com.impl.PersistentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class PersistentStorageTest {

    @Test
    @DisplayName("Read write 100 items from storage instance")
    void  testReadWriteFromStorage() {
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageRWTest.txt");
        int numNodes = 100;

        System.out.println("[TEST] Writing values to storage map");
        for (int i = 0; i < numNodes; i++) {
            persistentStorage.put(String.format("entry%d", i), i);
        }

        System.out.println("[TEST] Checking values from same storage map");
        for (int i = 0; i < numNodes; i++) {
            assertEquals(i, persistentStorage.get(String.format("entry%d", i)));
        }
        assertEquals(50, persistentStorage.get("entry50"));
    }

    @Test
    @DisplayName("Put 5 different items in storage and check them")
    @Tag("fast")
    void testPut(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStoragePutTest.txt");

        persistentStorage.put("randomName0", "randomStringValue");
        persistentStorage.put("randomName1", 0x1456);
        persistentStorage.put("randomName2", 1.3f);
        persistentStorage.put("randomName3", 4.856332323232d);
        persistentStorage.put("randomName4", 'a');

        //Normal types
        assertEquals( "randomStringValue",persistentStorage.get("randomName0"));
        assertEquals(0x1456,persistentStorage.get("randomName1"));
        assertEquals(1.3f,persistentStorage.get("randomName2"));
        assertEquals(4.856332323232d,persistentStorage.get("randomName3"));
        assertEquals('a',persistentStorage.get("randomName4"));
    }

    @Test
    @DisplayName("Get 5 different items, already written to DB")
    @Tag("fast")
    void testGet() {
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStoragePutTest.txt");

        assertEquals( "randomStringValue",persistentStorage.get("randomName0"));
        assertEquals(0x1456,persistentStorage.get("randomName1"));
        assertEquals(1.3f,persistentStorage.get("randomName2"));
        assertEquals(4.856332323232d,persistentStorage.get("randomName3"));
        assertEquals('a',persistentStorage.get("randomName4"));
    }

    @RepeatedTest(2)
    @DisplayName("Remove items")
    @Tag("fast")
    void testRemove(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageRemoveTest.txt");
        PersistentStorage persistentStorage1 = new PersistentStorage("PersistentStorageRemoveTest.txt");

        persistentStorage.put("randomName0", "randomStringValue");
        persistentStorage.remove("randomName0");
        assertEquals(null, persistentStorage.get("randomName0"));
        assertEquals(null, persistentStorage1.get("randomName0"));

        //Check if method returns false if remove failed
        assertEquals(false, persistentStorage1.remove("randomName0"), "Method must return false");
        assertEquals(false, persistentStorage.remove("randomName0"), "Method must return false");
    }

    @RepeatedTest(2)
    @DisplayName("Check contain method")
    @Tag("fast")
    void testContains(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageContTest.txt");
        persistentStorage.put("randomName0", "randomStringValue");
        PersistentStorage persistentStorage1 = new PersistentStorage("PersistentStorageContTest.txt");
        assertEquals(true, persistentStorage1.contains("randomName0"));
    }

    @Test
    @DisplayName("Write multiple times, read from another instance, check for persistence")
    @Tag("persistence")
    void testPersistence () {
        int numNodes = 681;
        PersistentStorage persistentStorageWrite = new PersistentStorage("PersistentStoragePersTest.txt");
        PersistentStorage persistentStorageRead = new PersistentStorage("PersistentStoragePersTest.txt");

        for (int i = 0; i < numNodes; i++) {
            if(i%2 == 0)
                persistentStorageWrite.put(String.format("entry%d", i), i);
            else
                persistentStorageWrite.put(String.format("entry%d", i), String.format("value%d", i));
        }

        for (int i = 0; i < numNodes; i++) {
            if(i%2 == 0)
                assertEquals(i,persistentStorageRead.get(String.format("entry%d", i)));
            else
                assertEquals(String.format("value%d", i),persistentStorageRead.get(String.format("entry%d", i)));
        }
    }
}

