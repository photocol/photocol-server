import junit.framework.TestCase;
import photocol.store.UserStore;

public class PhotocolTest extends TestCase {
    public void testPhotocol() {
        assertEquals(new UserStore().getUsers().length, 2);
        assertEquals(new UserStore().getUsers().length, 3);
    }
}
