import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.impl.PersistentStorage;

public class TestPersistentStorage {

    class TestClass{
        public final String testValue = "test";
        public final Integer testInt = 10;
    }

    @Test
    void  readWriteFromStorage() {
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageDB.txt");
        int numNodes = 100;

        for (int i = 0; i < numNodes; i++) {
            persistentStorage.put(String.format("entry%d", i), i);
        }

        for (int i = 0; i < numNodes; i++) {
            assertEquals(i, persistentStorage.get(String.format("entry%d", i)));
        }
        assertEquals(50, persistentStorage.get("entry50"));
    }

    @Test
    void testPut(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStoragePutTest.txt");

        persistentStorage.put("randomName0", "randomStringValue");
        persistentStorage.put("randomName1", 0x1456);
        persistentStorage.put("randomName2", 1.3f);
        persistentStorage.put("randomName3", 4.856332323232d);
        persistentStorage.put("randomName4", 'a');
        persistentStorage.put("randomName5", new TestClass());

        //Normal types
        assertEquals( "randomStringValue",persistentStorage.get("randomName0"));
        assertEquals(0x1456,persistentStorage.get("randomName1"));
        assertEquals(1.3f,persistentStorage.get("randomName2"));
        assertEquals(4.856332323232d,persistentStorage.get("randomName3"));
        assertEquals('a',persistentStorage.get("randomName4"));

        //Classes internal public vars
        assertEquals((Integer)10, ((TestClass) persistentStorage.get("randomName5")).testInt);
        assertEquals("test", ((TestClass) persistentStorage.get("randomName5")).testValue);
    }

    @Test
    void testGet() {
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageGetTest.txt");

        assertEquals( "randomStringValue",persistentStorage.get("randomName0"));
        assertEquals(0x1456,persistentStorage.get("randomName1"));
        assertEquals(1.3f,persistentStorage.get("randomName2"));
        assertEquals(4.856332323232d,persistentStorage.get("randomName3"));
        assertEquals('a',persistentStorage.get("randomName4"));
    }

    @Test
    void testRemove(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageRemoveTest.txt");
        PersistentStorage persistentStorage1 = new PersistentStorage("PersistentStorageRemoveTest.txt");

        persistentStorage.put("randomName0", "randomStringValue");
        persistentStorage.remove("randomName0");
        assertEquals(null, persistentStorage.get("randomName0"));
        assertEquals(null, persistentStorage1.get("randomName0"));
    }

    @Test
    void testContains(){
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageContTest.txt");
        persistentStorage.put("randomName0", "randomStringValue");
        PersistentStorage persistentStorage1 = new PersistentStorage("PersistentStorageContTest.txt");
        assertEquals(true, persistentStorage1.contains("randomName0"));
    }

    @Test
    void testPersistence () {
        int numNodes = 1000;
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

