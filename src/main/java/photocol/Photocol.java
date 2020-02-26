/* Main class for the Photocol app */

package photocol;

import photocol.store.CollectionStore;
import photocol.store.PhotoStore;
import photocol.store.UserStore;

public class Photocol {
    public static void main(String[] args) {
        new Endpoints(new UserStore(), new PhotoStore(), new CollectionStore());
    }
}
