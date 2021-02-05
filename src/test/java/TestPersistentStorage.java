import static org.junit.jupiter.api.Assertions.assertEquals;

import com.Temp;
import org.junit.jupiter.api.Test;
import com.PersistentStorage;

public class TestPersistentStorage {
    private PersistentStorage persistentStorage = new PersistentStorage("PersistentStorageDB.txt");

    @Test
    void  readWriteFromStorage(){
        Temp tmp  = new Temp();
        for(int i = 0; i<100; i++){
            persistentStorage.put(String.format("entry%s",i), 12368+10/3*i);
            if (i%50 == 0) persistentStorage.put("objectInTheMiddle",tmp );
        }
        Object obj = persistentStorage.get("objectInTheMiddle");
    }
}
