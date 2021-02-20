import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.impl.PersistentStorage;

public class TestPersistentStorage {

    @Test
    void  readWriteFromStorage() {
        PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageDB.txt");
        int numNodes = 100;

        for (int i = 0; i < numNodes; i++) {
            persistentStorage.put(String.format("entry%d", i), i);
            //if (i == 50) persistentStorage.put("objectInTheMiddle", 50);
        }
        Object obj = persistentStorage.get("objectInTheMiddle");

        for (int i = 0; i < numNodes; i++) {
            //JSON deserialize
            assertEquals(i, persistentStorage.get(String.format("entry%d", i)));
        }

    }

    @Test
    void testPesistence () {

       // PersistentStorage persistentStorage1 = new PersistentStorage("testPersistence.txt");

        // write some values
        // stop using instancce 1

       // PersistentStorage persistentStorage2 = new PersistentStorage("PersistentStorageDB.txt");

        // check if the previosly written values are here
    }
}
